/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.io.{File, FileReader, FileWriter,
                FileOutputStream, BufferedOutputStream,
                IOException, RandomAccessFile} 
import java.net.URL
import java.nio.channels.FileLock
import java.util.zip.{ZipFile,ZipEntry} 
import java.util.Enumeration
import scala.collection.immutable._ 
import scala.xml._ 

import ProposedChanges._
import sbaz.download.Downloader
import sbaz.download.Download._
import sbaz.keys._


/** <p>
 *    <code>ManagedDirectory</code> manages one directory of installed
 *    packages.
 *  </p>
 *  <p>
 *    It enforces the dependencies between packages: There is never
 *    a set of installed packages where a fully installed package
 *    does not have its dependents installed.
 *  </p>
 *
 *  @author Lex Spoon
 */
class ManagedDirectory(val directory: File) {

  private val lib_dir  = new File(directory, "lib")
  private val meta_dir = new File(directory, "meta")
  private val misc_dir = new File(directory, "misc")

  // check that the directory looks valid
  if (!lib_dir.isDirectory && !meta_dir.isDirectory && !misc_dir.isDirectory)
    throw new Error("Directory " + directory + 
                    " does not appear to be a sbaz-managed directory")

  // Obtain lock on "meta/.lock" file to prevent concurrent updates. It will be
  // released on exit of the JVM. Java API allows for null value on failure.
  val lock = {
    val lockFile = new File(meta_dir, ".lock")
    lockFile.deleteOnExit()
    new RandomAccessFile(lockFile, "rw").getChannel.tryLock
  }
  if (lock == null || !lock.isValid)
    throw new Error("Directory " + directory + " is locked by another process.")

  private val downloader = Downloader(new File(meta_dir, "cache")) 

  // Rename a file.  Don't use <code>renameTo()</code>, because on Windows
  // it refuses to overwrite the target file.
  private def renameFile(from: File, to: File) {
    to.delete()
    from.renameTo(to)
  }

  /** Load an XML doc from the specified filename. */
  private def loadXML[T](filename: String, decoder: Node => T, default: T): T = {
    val file = new File(meta_dir, filename)

    if (file.exists())
      decoder(XML.load(file.getAbsolutePath()))
    else
      default
  }

  /** Save an XML node to a file in the meta directory, being
   *  careful to do it in a transactional style: first create
   *  a tmp file, then rename the tmp file to the original.
   *  If the underling renameTo() routine is atomic, then
   *  at no time is the underlying file incomplete or missing.
   *  XXX It isn't.  Thus, the way all the file swizling happens
   *  needs to be rethought.
   */
  private def saveXML(xml: Node, filename: String) {
    val tmpFile = new File(meta_dir, filename + ".tmp")
    val str = new FileWriter(tmpFile)
    str.write(xml.toString())
    str.close()
    renameFile(tmpFile, new File(meta_dir, filename))
  }

  // Load the list of available packages
  var available: AvailableList = 
    loadXML("available",
	    AvailableListUtil.fromXML,
	    new AvailableList(Nil))

  private def saveAvailable() =
    saveXML(available.toXML,
	    "available")

  // Load the list of installed packages
  val installed: InstalledList  =  
    loadXML("installed",
	    InstalledList.fromXML,
	    new InstalledList())

  private def saveInstalled() =
    saveXML(installed.toXML,
	    "installed")

  // load the universe specification from the directory 
  var universe: Universe = Universe.fromFile(
    new File(meta_dir, "universe"))
  universe.keyringFilesAreIn(meta_dir)

  private def saveUniverse() =
    saveXML(universe.toXML, "universe")

  // forget the notion of available files
  private def clearAvailable() = {
    available = new AvailableList(Nil)
    (new File(meta_dir, "available")).delete()
  }

  def setUniverse(newUniverse: Universe) {
    clearAvailable()

    universe = newUniverse
    saveUniverse()
  }
 
  /**
   * Parse a zip-ish "/" delimited file name into a relative Filename. Unlike
   * the zipToFilename function, the path is left "as is". When extracting zip
   * contents, an intermediary filename may be needed for packaging specific
   * processing. This name is not tracked beyond the install process.
   * @param ent A ZipEntry
   * @return Filename of a direct translation of the ent path
   */
  private def zipToOutputFilename(ent: ZipEntry): Filename = {
    val pathParts = ent.getName().split("/").toList.filter(s => s.length() > 0) 
    new Filename(!ent.isDirectory, true, pathParts)
  }

  /**
   * Parse a zip-ish "/"-delimited filename into a relative Filename for
   * installation into the managed directory. If needed, the resulting Filename
   * can be different from the name contained within the ZipEntry.
   * @param ent A ZipEntry
   * @return Filename of the relative install path for the extracted file.
   */
  private def zipToFilename(ent: ZipEntry): Filename = {
    val pathParts = {
      if (isPack200(ent)) {
        val name = ent.getName().substring(0, ent.getName().lastIndexOf('.')) + ".jar"
        name.split("/").toList.filter(s => s.length() > 0)
      }
      else ent.getName().split("/").toList.filter(s => s.length() > 0)
    }
    new Filename(!ent.isDirectory, true, pathParts)
  }

  /** 
   * Try to make a file executable.  This routine runs <code>chmod +x</code>.
   * If <code>chmod</code> cannot be found, it fails quietly.
   */
  private def makeExecutable(file: File) =
    try {
      Runtime.getRuntime().exec(Array("chmod", "+x", file.getPath()))
    } catch {
      case _:java.io.IOException => ()
    }
 
  /** 
   * Make a series of changes after auditing for completeness.  No change to
   * the managed directory should be made without all audits passing with
   * success. The ProposedChanges should be complete, as no dependency resolution
   * is performed here.
   */
  def makeChanges(changes: Seq[ProposedChange]): Unit = {
    // XXX: Allow for manual override (-f option or interactive)
    // XXX: May be an extension point for multiple UI solutions
    // XXX: An audit pipeline, pre/post processing, configure step, etc could go here.
    auditProposedChangeDependencies(installed, changes).foreach(
      msg => throw new DependencyError(msg)
    )

    // download necessary files
    val dnlResults = downloader.download( extractAvailablePackages(changes) )
    auditDownloadResults(dnlResults).foreach( msg => throw new DependencyError(msg))

    // make sure installable package contents do not collide
    auditPackagesForFileCollisions(installed, changes, dnlResults).foreach(
      msg => throw new DependencyError(msg)
    )

    // do removals first, in case file contents are moved between packages
    def rmForUpdate(packName: String) {
      installed.entryNamed(packName).foreach( entry => removeNoCheck(entry) )
    }
    for (change <- changes.iterator) {
      change match {
        case Removal(spec) => rmForUpdate(spec.name)
        case AdditionFromNet(avail) => rmForUpdate(avail.pack.name)
        case change@AdditionFromFile(file) => rmForUpdate(change.pack.name)
      }
    }
    
    // now do the installs
    for (change <- changes.iterator) {
      change match {
        case Removal(spec) => ()  // already done
        case AdditionFromNet(avail) => installNoCheck(avail.pack, dnlResults(avail).get)
        case change@AdditionFromFile(file) => installNoCheck(change.pack, file)
      }
    }
  }

  def auditProposedChangeDependencies(
          installedList: InstalledList, 
          changes: Seq[ProposedChange]): Option[String] = {
    // XXX: Improve feedback about what packages are causing the breakage
    val broken = installedList.identifyBreakingChanges(changes)
    if (!broken.isEmpty) {
      val message = "Action aborted due to broken dependencies.\n"
      Some(broken.foldLeft(message){(message, broke) => 
        message + "\t" + broke._1 + " depends on:\n" + broke._2.foldLeft(""){ 
          (str, dep) => str + "\t\t" + dep + "\n" 
        }
      })
    } 
    else None
  }
  
  def auditDownloadResults(dnlResults: Map[AvailablePackage, FinalStatus]): Option[String] = {
    val dnlFails = dnlResults.keysIterator.filter(key => {
      dnlResults(key).isInstanceOf[sbaz.download.Download.Fail]
    })
    if (dnlFails.hasNext){
      val message = "Required dependencies could not be downloaded:\n"
      Some(dnlFails.foldLeft(message)((message, fail) => {
        message + "\t" + fail + ": " + dnlResults.get(fail).get + "\n"
      }))
    } 
    else None
  }

  def auditPackagesForFileCollisions(
          installedOrig: InstalledList, 
          changes: Seq[ProposedChange],
          dnlResults: Map[AvailablePackage, FinalStatus]): Option[String] = {

    // Work on copy of installed list, as this is a dry run
    val installed = new InstalledList()
    installed addAll installedOrig.installedEntries

    // do removals first to avoid irrelevant collisions
    def rmForUpdate(packName: String) {
      installed.entryNamed(packName).foreach( entry => 
        installed.remove(entry.packageSpec))
    }
    for (change <- changes.iterator) {
      change match {
        case Removal(spec) => installed.remove(spec)
        case AdditionFromNet(avail) => rmForUpdate(avail.pack.name)
        case change@AdditionFromFile(file) => rmForUpdate(change.pack.name)
      }
    }

    val collisionMap = changes.foldRight[Map[Package, List[InstalledEntry]]](Map.empty) ((change, map) => {
      change match {
        case Removal(spec) => map
        case change@AdditionFromNet(avail) => {
          val collisions = findCollisions(installed, change.pack, dnlResults(avail).get)
          if (collisions.isEmpty) map
          else map + (change.pack -> collisions)
        }
        case change@AdditionFromFile(file) => {
          val collisions = findCollisions(installed, change.pack, file)
          if (collisions.isEmpty) map
          else map + (change.pack -> collisions)
        }
      }
    })
    
    if (!collisionMap.isEmpty) {
      val message = "Action aborted due to inter-package content collisions.\n"
      val txt = collisionMap.keysIterator.foldRight[String](message)( (key, m1) => {
        m1 + "\t" + key.toString + " collides with:\n" +
        collisionMap(key).foldRight[String]("")( (entry, m2) => {
          m2 + "\t\t" + entry.packageSpec.toString + "\n"
        })
      })
      Some(txt)
    } 
    else None
  }

  /**
   * Identify installed entries whose contents collide with a package's file contents
   * @param installed An InstalledList to audit against
   * @param pack The Package in question
   * @param file The package's file whose contents will be interrogated
   * @return A List of InstalledEntry objects that collide with the package's file contents
   */
  def findCollisions(installed: InstalledList, pack: Package, file: File): List[InstalledEntry] = {
    val zip = new ZipFile(file)
    val zipEntsAll = mkList(zip.entries().asInstanceOf[Enumeration[ZipEntry]])
    val zipEntsToInstall = zipEntsAll.filter(e => !(e.getName().startsWith("meta/")))
    val collisions = zipEntsToInstall.foldRight[List[InstalledEntry]](Nil)( (ent, list) => {
      if (ent.isDirectory) list
      else list ::: installed.entriesWithFile(zipToFilename(ent))
    })
    installed.add(new InstalledEntry(pack, zipEntsToInstall.map(zipToFilename)))
    collisions.removeDuplicates
  }
    
  /** Turn a sequence of ProposedChanges into a list of AvailablePackages */
  private def extractAvailablePackages(changes: Seq[ProposedChange]) = {
    changes.iterator.foldLeft[List[AvailablePackage]](List()) {
      (list, change) => change match { 
        case AdditionFromNet(avail) => avail :: list 
        case _ => list 
      }
    }
  }
  
  /** Turn an Enumeration into a List */
  private def mkList[A](enum: Enumeration[A]): List[A] = {
    var l: List[A] = Nil 
    while (enum.hasMoreElements()) {
      val n = enum.nextElement().asInstanceOf[A] 
      l = n :: l 
    }  
	
    l.reverse
  }

  private val isWin = System.getProperty("os.name") startsWith "Windows"
  private val sbaz_jar = new File(misc_dir, "sbaz" + File.separator + "sbaz.jar")
  private val scala_lib_jar = new File(misc_dir, "sbaz" + File.separator + "scala-library.jar")

  /** 
   * The installation of some files on the Windows platform can't be
   * performed when the JVM is running; their installation is thus delayed
   * and handled by the executable batch file.
   */
  private def isSpecial(f: File): Boolean =
   isWin && (
   f.compareTo(sbaz_jar) == 0 ||
   f.compareTo(scala_lib_jar) == 0)
  
  /**
   * Jar files can be compressed more aggressively with Pack200, which is 
   * available starting in Java 1.5. Note that the ZipEntry should only
   * have Pack200 applied without gzip compression, as the SBP file is
   * already compressed using zip.
   */
  private def isPack200(e: ZipEntry): Boolean = e.getName.endsWith(".pack")

  /** Extract entries from a zip file into a specified directory. */
  def extractFiles(zip: ZipFile, entries: List[ZipEntry], directory: File) {
    for (ent <- entries) {
      val file: File = {
        val f = zipToOutputFilename(ent).relativeTo(directory)
        if (isSpecial(f)) new File(f.getParentFile, f.getName + ".staged")
        else f
      }
      if (ent.isDirectory()) {
        file.mkdirs()
      } else {
        if (file.getParent() != null)
          file.getParentFile().mkdirs()

        val in = zip.getInputStream(ent) 
        val out = new FileOutputStream(file)

        val buf = new Array[Byte](8192)
        def lp() {
          val len = in.read(buf) 
          if (len >= 0) {
            out.write(buf, 0, len)
            lp()
          }
        }
        lp()

        in.close()
        out.close()

        // For some reason, unpacking directly from the ZipFile's input stream 
        // creates incomplete output files.  Extracting the file from the zip
        // and then performing unpack seems to work properly
        if (isPack200(ent)) {
          val unpackedFile: File = {
            val f = zipToFilename(ent).relativeTo(directory)
            if (isSpecial(f)) new File(f.getParentFile, f.getName + ".staged")
            else f
          }
          unpack200(file, unpackedFile)
          file.delete()
        } 

        if (ent.getName() startsWith "bin/")
          makeExecutable(file)
      }
    }
  }
  
  /** Unpacks the Pack200 in file to the out file. */
  private def unpack200(in: File, out: File) {
    import java.util.jar.{Pack200, JarOutputStream}
    val unpacker = Pack200.newUnpacker()
    val jout = new JarOutputStream(new FileOutputStream(out))
    unpacker.unpack(in, jout)
    jout.close()
  }

  /** 
   * Install package into the managed directory without any audits. This
   * should not be called directly in the normal case.  Use makeChanges(...)
   * instead.
   */
  def installNoCheck(pack: Package, downloadedFile: File) {
    val zip = new ZipFile(downloadedFile)

    val zipEntsAll = mkList(zip.entries().asInstanceOf[Enumeration[ZipEntry]])
    val zipEntsToInstall =
      zipEntsAll.filter(e => !(e.getName().startsWith("meta/")))

    // check if any package already includes files
    // in the new package
    // XXX: This should be removed in favor of audits in makeChanges(...)
    for{ent <- zipEntsToInstall
        if !ent.isDirectory()
        conf <- installed.entriesWithFile(zipToFilename(ent))
        if conf.name != pack.name}
    {
       throw new DependencyError("package " + conf.packageSpec +
                                 " already includes " + ent.getName())
    }

    // create a new entry 
    val installedFiles = zipEntsToInstall.map(zipToFilename)
    val newEntry = new InstalledEntry(pack, installedFiles)

    // uninstall files from the existing same-named package,
    // if there is one
    installed.entryNamed(pack.name) match {
      case None => ()
      case Some(entry) =>
        removeEntryFiles(entry)
    }

    // finally, install the new files and add the package entry
    extractFiles(zip, zipEntsToInstall, directory)
    installed.add(newEntry)
    saveInstalled()

    // clean up
    zip.close()
  }

  // install a package from the web
  def install(pack: AvailablePackage) {
    makeChanges(List(AdditionFromNet(pack)))
  }

  // Install a package from a file.  It must be a zip file
  // that includes its metadata in the zip entry "meta/description".
  def install(file: File) {
    if(!file.exists) throw new java.io.FileNotFoundException(file.getAbsolutePath)
    makeChanges(List(AdditionFromFile(file)))
  }

  // delete the files associated with an installed package
  private def removeEntryFiles(entry: InstalledEntry) {
    val fullFiles = entry.files.map(f => f.relativeTo(directory))

    // Sort the files, so that items get deleted before their
    // parent directories do.
    val sortedFiles = fullFiles.sortWith((a,b) => a.getAbsolutePath() >= b.getAbsolutePath())

    def cleanupEmptyDirs(file: File) {
      if(directory != file && file.delete)
        cleanupEmptyDirs(file.getParentFile)
    } 

    for (f <- sortedFiles if f.exists && !isSpecial(f)) {
      val succ = f.delete()
      if (!succ) {
        if (!f.isDirectory)
          throw new IOException("could not delete " + f)
      } else cleanupEmptyDirs(f.getParentFile)
    }
  }

  def remove(entry: InstalledEntry) {
    if (installed.anyDependOn(entry.name))
      throw new DependencyError("Package " + entry.name + " is still needed")
      
    removeNoCheck(entry)
  }

  private def removeNoCheck(entry: InstalledEntry) {
    removeEntryFiles(entry)
    installed.remove(entry.packageSpec)
    saveInstalled()
  }

  // retrieve a fresh list of available packages from the network
  def updateAvailable() {
    available = universe.retrieveAvailable()
    saveAvailable()
  }

  // Compact the directory, removing any unnecessary files.  Specifically,
  // delete all downloaded files from meta/cache
  def compact {
    downloader.flushCache
  }

  override def toString() =
    ("(" + directory.toString() + ": " +
         installed.size + "/" +
         available.numPackages + " packages)")
}
