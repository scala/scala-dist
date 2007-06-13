/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.io.{File, FileReader, FileWriter,
                FileOutputStream, BufferedOutputStream,
                IOException} 
import java.net.URL
import java.util.zip.{ZipFile,ZipEntry} 
import scala.collection.immutable._ 
import scala.xml._ 
import ProposedChanges._
import sbaz.keys._


// ManagedDirectory manages one directory of installed packages.

// It enforces the dependencies between packages: There is never
// a set of installed packages where a fully installed package
// does not have its dependents installed.

class ManagedDirectory(val directory : File)
{
  def this(directory: File, miscDirectory: File) = this(directory)
  val meta_dir = new File(directory, "meta") 
  val old_meta_dir = new File(directory, "scbaz") 

  // check that the directory looks valid
  if (!meta_dir.isDirectory() && !old_meta_dir.isDirectory())
    throw new Error("Directory " + directory + 
                    " does not appear to be a sbaz-managed directory")

  // if the directory has an scbaz subdir instead of meta,
  // change to the new name
  if (old_meta_dir.exists() && !meta_dir.exists())
    old_meta_dir.renameTo(meta_dir)

  val downloader = new Downloader(new File(meta_dir, "cache")) 

  // Rename a file.  Don't use renameTo(), because on Windows
  // it refuses to overwrite the target file.
  private def renameFile(from: File, to: File) = {
    to.delete()
    from.renameTo(to)
  }

  // Load an XML doc from the specified filename.
  // The routine looks in meta_dir followed by old_meta_dir.
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

  // Save an XML node to a file in the meta directory, being
  // careful to do it in a transactional style: first create
  // a tmp file, then rename the tmp file to the original.
  // If the underling renameTo() routine is atomic, then
  // at no time is the underlying file incomplete or missing.
  // XXX It isn't.  Thus, the way all the file swizling happens
  // needs to be rethought.
  private def saveXML(xml: Node,
		      filename: String) =
  {
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
  var universe : Universe = Universe.fromFile(
    new File(meta_dir, "universe"))
  universe.keyringFilesAreIn(meta_dir)

  private def saveUniverse() =
    saveXML(universe.toXML, "universe")

  // forget the notion of available files
  private def clearAvailable() = {
    available = new AvailableList(Nil)
    (new File(meta_dir, "available")).delete()
  }

  def setUniverse(newUniverse: Universe) = {
    clearAvailable()

    universe = newUniverse
    saveUniverse()
  }

  // download a file if it isn't already downloaded,
  // and return the name of the downloaded file
  private def download(avail: AvailablePackage): File = {
    val basename = avail.filename
    if (!downloader.is_downloaded(basename))
      downloader.download(avail.link, basename)
      
    new File(downloader.dir, basename)
  }
  
  // parse a zip-ish "/"-delimited filename into a relative Filename
  private def zipToFilename(ent: ZipEntry): Filename = {
    val pathParts = ent.getName().split("/").toList.filter(s => s.length() > 0) 
    new Filename(!ent.isDirectory, true, pathParts)
  }


  // Try to make a file executable.  This routine runs chmod +x .
  // If chmod cannot be found, it fails quietly.
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
    for (val AdditionFromNet(avail) <- changes.elements)
      download(avail)

    // do removals first, in case some of the additions are upgrades
    for (val Removal(spec) <- changes.elements;
         val entry <- installed.entryWithSpec(spec))
      removeNoCheck(entry)
    
    // now do additions
    for (val change <- changes.elements) {
      change match {
        case Removal(spec) => ()  // already done
        case AdditionFromNet(avail) => installNoCheck(avail.pack, download(avail))
        case change@AdditionFromFile(file) => installNoCheck(change.pack, file)
      }
    }
  }

  // turn an Enumeration into a List
  private def mkList[A](enum: java.util.Enumeration) : List[A] = {
    var l: List[A] = Nil 
    while (enum.hasMoreElements()) {
      val n = enum.nextElement().asInstanceOf[A] 
      l = n :: l 
    }  
	
    l.reverse
  }



  // Extract entries from a zip file into a specified directory.
  def extractFiles(zip:ZipFile, entries: List[ZipEntry], directory:File) = {
    for (val ent <- entries)
    {
      val file: File = zipToFilename(ent).relativeTo(directory)
	
      if (ent.isDirectory()) {
        file.mkdirs()
      } else {
        if (file.getParent() != null)
          file.getParentFile().mkdirs()
	
          val in = zip.getInputStream(ent) 
          val out = new BufferedOutputStream(new FileOutputStream(file))
      	
      	
          val buf = new Array[byte](1024)
          def lp() : Unit = {
          val len = in.read(buf) 
          if (len >= 0) {
            out.write(buf, 0, len)
            lp()
          }
        }
        lp()
      	
        in.close()
        out.close()
	
        if (ent.getName().startsWith("bin/"))
          makeExecutable(file)
      }
    }
  }

  
  def installNoCheck(pack: Package, downloadedFile: File): Unit = {
    val zip = new ZipFile(downloadedFile)

    val zipEntsAll = mkList[ZipEntry](zip.entries())
    val zipEntsToInstall =
      zipEntsAll.filter(e => !(e.getName().startsWith("meta/")))



    // check if any package already includes files
    // in the new package
    for{val ent <- zipEntsToInstall
        !ent.isDirectory()
        val conf <- installed.entriesWithFile(zipToFilename(ent))
        conf.name != pack.name}
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
      case Some(entry) => {
        removeEntryFiles(entry)
      }
    }


    // finally, install the new files and add the package entry
    extractFiles(zip, zipEntsToInstall, directory)
    installed.add(newEntry)
    saveInstalled()

    // clean up
    zip.close()
  }

  // install a package from the web
  def install(pack : AvailablePackage): Unit =
    makeChanges(List(AdditionFromNet(pack)))


  // Install a package from a file.  It must be a zip file
  // that includes its metadata in the zip entry "meta/description".
  def install(file: File): Unit =
    makeChanges(List(AdditionFromFile(file)))


  // delete the files associated with an installed package
  private def removeEntryFiles(entry:InstalledEntry) = {
    val fullFiles =
      entry.files.map(f => f.relativeTo(directory))

    // Sort the files, so that items get deleted before their
    // parent directories do.
    val sortedFiles = fullFiles.sort((a,b) => a.getAbsolutePath() >= b.getAbsolutePath())


    for (val f <- sortedFiles; f.exists) {
      val succ = f.delete()
      if(!succ) {
        if(!f.isDirectory)
          throw new IOException("could not delete " + f)
      }
    }
  }

  def remove(entry:InstalledEntry) = {
    if (installed.anyDependOn(entry.name))
      throw new DependencyError("Package " + entry.name + " is still needed")
      
    removeNoCheck(entry)
  }
  
  private def removeNoCheck(entry: InstalledEntry) = {
    removeEntryFiles(entry)
    installed.remove(entry.packageSpec)
    saveInstalled()
  }

  // download a URL to a file
  private def downloadURL(url: URL, file: File) = {
    val connection = url.openConnection()
    val inputStream = connection.getInputStream()

    val f = new java.io.FileOutputStream(file)
    def lp():Unit = {
      val dat = new Array[byte](100)
      val numread = inputStream.read(dat)
      if(numread >= 0) {
	f.write(dat,0,numread)
	lp()
      }
    }
    lp()
    f.close()
  } 

  // retrieve a fresh list of available packages from the network
  def updateAvailable() = {
    available = universe.retrieveAvailable()
    saveAvailable()
  }

  // Compact the directory, removing any unnecessary files.  Specifically,
  // delete all downloaded files from meta/cache
  def compact = {
    downloader.flushCache
  }

  override def toString() =
    ("(" + directory.toString() + ": " +
         installed.size + "/" +
         available.numPackages + " packages)")
}
