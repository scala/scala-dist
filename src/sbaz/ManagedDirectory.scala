/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.io.{File, FileReader, FileWriter,
                FileOutputStream, BufferedOutputStream,
                IOException} 
import java.net.URL
import java.util.zip.{ZipFile,ZipEntry} 
import java.util.Enumeration
import scala.collection.immutable._ 
import scala.xml._ 

import ProposedChanges._
import sbaz.download.Downloader
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

  private val downloader = Downloader(new File(meta_dir, "cache")) 

  // Rename a file.  Don't use <code>renameTo()</code>, because on Windows
  // it refuses to overwrite the target file.
  private def renameFile(from: File, to: File) {
    to.delete()
    from.renameTo(to)
  }

  /** Load an XML doc from the specified filename. */
  private def loadXML[T](filename: String,
			 decoder: Node => T,
		         default: T)
                        : T =
  {
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
 
  // When extracting zip contents, an intermediary filename may be needed.
  // This name is not tracked beyond the install process.
  private def zipToOutputFilename(ent: ZipEntry): Filename = {
    val pathParts = ent.getName().split("/").toList.filter(s => s.length() > 0) 
    new Filename(!ent.isDirectory, true, pathParts)
  }

  // parse a zip-ish "/"-delimited filename into a relative Filename
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

  /** Try to make a file executable.  This routine runs <code>chmod +x</code>.
   *  If <code>chmod</code> cannot be found, it fails quietly.
   */
  private def makeExecutable(file: File) =
    try {
      Runtime.getRuntime().exec(Array("chmod", "+x", file.getPath()))
    } catch {
      case _:java.io.IOException => ()
    }
 
  // make a series of changes
  def makeChanges(changes: Seq[ProposedChange]): Unit = {
    // check that the changes maintain dependencies
    if (!installed.changesAcceptible(changes))
      throw new DependencyError()

    // download necessary files
    val dnlResults = downloader.download( extractAvailablePackages(changes) )

    //TODO: abort if file could not be downloaded
    
    //TODO: make sure installable package contents do not collide
    //if (!installed.changesExplodedAcceptible(

    // do removals first, in case some of the additions are upgrades
    for (Removal(spec) <- changes.iterator;
         entry <- installed.entryWithSpec(spec))
      removeNoCheck(entry)

    // now do additions
    for (change <- changes.iterator) {
      change match {
        case Removal(spec) => ()  // already done
        case AdditionFromNet(avail) => installNoCheck(avail.pack, dnlResults(avail).get)
        case change@AdditionFromFile(file) => installNoCheck(change.pack, file)
      }
    }
  }

  // turn a sequence of ProposedChanges into a list of AvailablePackages
  private def extractAvailablePackages(changes: Seq[ProposedChange]) = {
    changes.iterator.foldLeft[List[AvailablePackage]](List()) {
      (list, change) => change match { 
        case AdditionFromNet(avail) => avail :: list 
        case _ => list 
      }
    }
  }
  
  // turn an Enumeration into a List
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

  /** The installation of some files on the Windows platform can't be
   *  performed when the JVM is running; their installation is thus delayed
   *  and handled at the end of the corresponding batch file.
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

  // Extract entries from a zip file into a specified directory.
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

  def installNoCheck(pack: Package, downloadedFile: File) {
    val zip = new ZipFile(downloadedFile)

    val zipEntsAll = mkList(zip.entries().asInstanceOf[Enumeration[ZipEntry]])
    val zipEntsToInstall =
      zipEntsAll.filter(e => !(e.getName().startsWith("meta/")))

    // check if any package already includes files
    // in the new package
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

    for (f <- sortedFiles if f.exists && !isSpecial(f)) {
      val succ = f.delete()
      if (!succ) {
        if (!f.isDirectory)
          throw new IOException("could not delete " + f)
      }
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

  // download a URL to a file
  private def downloadURL(url: URL, file: File) {
    val connection = url.openConnection()
    val inputStream = connection.getInputStream()

    val f = new java.io.FileOutputStream(file)
    def lp() {
      val dat = new Array[Byte](100)
      val numread = inputStream.read(dat)
      if(numread >= 0) {
	f.write(dat, 0, numread)
	lp()
      }
    }
    lp()
    f.close()
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
