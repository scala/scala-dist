package sbaz;

import java.io.{File, FileReader, FileWriter,
                FileOutputStream, BufferedOutputStream} ;
import java.net.URL;
import java.util.zip.{ZipFile,ZipEntry} ;
import scala.collection.immutable._ ;
import scala.xml._ ;


// ManagedDirectory manages one directory of installed packages.

// It enforces the dependencies between packages: There is never
// a set of installed packages where a fully installed package
// does not have its dependents installed.

class ManagedDirectory(val directory : java.io.File) {
  val sbaz_dir = new File(directory, "scbaz") ;
  if(! sbaz_dir.exists()  ||  ! sbaz_dir.isDirectory()) {
    throw new Error("Directory " + directory + " does not appear to be a sbaz-managed directory");
  };

  var universe : Universe = new EmptyUniverse() ;
  var available : AvailableList = new AvailableList(Nil) ;
  var installed : InstalledList  =  new InstalledList() ;
  val downloader = new Downloader(new File(sbaz_dir, "cache")) ;

  private def loadAvailable() = {
    val file = new File(sbaz_dir, "available") ;

    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath()) ;
      available = AvailableListUtil.fromXML(node) ;
    } else {
      available = new AvailableList(Nil);
    }
  }
  loadAvailable();

  private def saveAvailable() = {
    val tmpFile = new File(sbaz_dir, "available.tmp");
    val str = new FileWriter(tmpFile);
    str.write(available.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(sbaz_dir, "available"));
  }


  private def loadInstalled() = {
    val file = new File(sbaz_dir, "installed") ;

    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath()) ;
      installed = InstalledList.fromXML(node) ;
    } else {
      installed = new InstalledList();
    }
  }
  loadInstalled();

  private def saveInstalled() = {
    val tmpFile = new File(sbaz_dir, "installed.tmp");
    val str = new FileWriter(tmpFile);
    str.write(installed.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(sbaz_dir, "installed"));
  }


  // load the universe specification from the directory 
  private def loadUniverse() = {
    val file = new File(sbaz_dir, "universe");
    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath());
      universe = Universe.fromXML(node);
    }
  }
  loadUniverse();

  private def saveUniverse() = {
    val tmpFile = new File(sbaz_dir, "universe.tmp");
    val str = new FileWriter(tmpFile);
    str.write(universe.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(sbaz_dir, "universe"));
  }


  // forget the notion of available files
  private def clearAvailable() = {
    available = new AvailableList(Nil);
    (new File(sbaz_dir, "available")).delete();
  }


  def setUniverse(newUniverse : Universe) = {
    clearAvailable();

    universe = newUniverse;
    saveUniverse();
  }


  // install a package that has been downloaded
  def install(pack: Package, downloadedFile: File): Unit = {
    // parse a zip-ish "/"-delimited filename into a relative File
    def zipToFile(name:String) : File = {
      val path_parts = name.split("/").toList.filter(s => s.length() > 0) ;
      val file = path_parts.foldLeft
                      (new File(""))
                      ((d,n) => new File(d,n)) ;
      file
    }

    // turn an Enumeration into a List
    def mkList[A](enum:java.util.Enumeration) : List[A] = {
      var l : List[A] = Nil ;
      while(enum.hasMoreElements()) {
	val n = enum.nextElement().asInstanceOf[A] ;
	l = n :: l ;
      }

      l.reverse
    }

    // Extract entries from a zip file into a specified directory.
    def extractFiles(zip:ZipFile, entries: List[ZipEntry], directory:File) = {
      for(val ent <- entries)
      {
	val file = new File(directory, zipToFile(ent.getName()).getPath()) ;
	if(ent.isDirectory()) {
	  file.mkdirs();
	} else {
	  val in = zip.getInputStream(ent) ;
	  val out = new BufferedOutputStream(new FileOutputStream(file));
	  
	  
	  val buf = new Array[byte](1024);
	  def lp() : Unit = {
	    val len = in.read(buf) ;
	    if(len >= 0) {
	      out.write(buf, 0, len);
	      lp();
	    }
	  }
	  lp();
	  
	  in.close();
	  out.close();
	}
      }
    }

    if(! installed.includesDependenciesOf(pack)) {
      // package's dependencies are not installed
      throw new DependencyError();
    }

    val zip = new ZipFile(downloadedFile);

    val zipEntsAll = mkList[ZipEntry](zip.entries());
    val zipEntsToInstall =
      zipEntsAll.filter(e => !(e.getName().startsWith("meta/")));

    // check if any package already includes files
    // in the new package
    for(val ent <- zipEntsToInstall;
	!ent.isDirectory();
	val conf <- installed.entriesWithFile(zipToFile(ent.getName()));
	conf.name != pack.name)
    {
      // XXX DependencyError ought to carry an explanation
      Console.println("package " + conf.packageSpec +
                      " already includes " + ent.getName() + "!");
      throw new DependencyError();
    }

    // create a new entry 
    val installedFiles = zipEntsToInstall.map(ent => zipToFile(ent.getName()));
    val newEntry = new InstalledEntry(pack.name,
				      pack.version,
				      installedFiles,
				      pack.depends) ;


    // uninstall files from the existing same-named package,
    // if there is one
    installed.entryNamed(pack.name) match {
      case None => ();
      case Some(entry) => {
	installed.add(entry.broken);  // leave this broken indicator,
	                              // so that the dependency
                                      // invariant stays intact
	saveInstalled();
	removeEntryFiles(entry);
      }
    }


    // finally, juggle carefully and install the new
    // files and package entry
    installed.add(newEntry.broken);
    saveInstalled();
    extractFiles(zip, zipEntsToInstall, directory);
    installed.add(newEntry.completed);
    saveInstalled();

    // clean up
    zip.close();
  }

  // install a package from the web
  def install(pack : AvailablePackage): Unit = { 
    if(! downloader.is_downloaded(pack.filename)) {
      downloader.download(pack.link, pack.filename)
    }

    install(pack.pack, new File(downloader.dir, pack.filename));
  }

  // Install a package from a file.  It must be a zip file
  // that includes its metadata in the zip entry "meta/description".
  def install(file: File): Unit = {
    val zip = new ZipFile(file);
    val ent = zip.getEntry("meta/description");
    if(ent == null)
      throw new Error("malformed package file: meta/description is missing");
    

    val inBytes = zip.getInputStream(ent);
    val packXML = XML.load(inBytes);
    inBytes.close();
    zip.close();

    val pack = PackageUtil.fromXML(packXML);

    install(pack, file)
  }


  // delete the files associated with an installed package
  private def removeEntryFiles(entry:InstalledEntry) = {
    val fullFiles =
      entry.files.map(f =>
	new File(directory, f.getPath())) ;

    // Sort the files, so that items get deleted before their
    // parent directories do.
    val sortedFiles = fullFiles.sort((a,b) => a.getPath() > b.getPath()) ;


    for(val f <- sortedFiles) {
      f.delete() ;
    }

  }

  def remove(entry:InstalledEntry) = {
    if(installed.anyDependOn(entry.name))
      // XXX there should be a DependencyError
      throw new Error("Package " + entry.name + " is still needed");

    installed.add(entry.broken);
    saveInstalled();

    removeEntryFiles(entry);

    installed.remove(entry.packageSpec);
    saveInstalled();
  }


  // download a URL to a file
  private def downloadURL(url:URL, file:File) = {
    val connection = url.openConnection();
    val inputStream = connection.getInputStream();

    val f = new java.io.FileOutputStream(file);
    def lp():Unit = {
      val dat = new Array[byte](100);
      val numread = inputStream.read(dat);
      if(numread >= 0) {
	f.write(dat,0,numread);
	lp();
      }
    }
    lp();
    f.close();
  } 

  // retrieve a fresh list of available packages from the network
  def updateAvailable() = {
    available = universe.retrieveAvailable();
    saveAvailable();
  }

  override def toString() = {
    "(" + directory.toString() + ": " +
        installed.packages.length + "/" +
        available.packages.length + " packages)"
  }

}
