package scbaz;

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
  val scbaz_dir = new File(directory, "scbaz") ;
  // XXX what if the scbaz directory doesn't exist?
  // option 1: have it automatically throw an error here
  // option 2: have a verify() method
  // option 3: have a ensureExists() method
  // multiple options possible; 2+3 looks reasonable

  var universe : Universe = new EmptyUniverse() ;
  var available : PackageSet = PackageSet.Empty ;
  var installed : InstalledList  =  new InstalledList() ;
  val downloader = new Downloader(new File(scbaz_dir, "cache")) ;

  private def loadAvailable() = {
    val file = new File(scbaz_dir, "available") ;

    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath()) ;
      available = PackageSet.fromXML(node) ;
    } else {
      available = PackageSet.Empty;
    }
  }
  loadAvailable();

  private def saveAvailable() = {
    val tmpFile = new File(scbaz_dir, "available.tmp");
    val str = new FileWriter(tmpFile);
    str.write(available.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(scbaz_dir, "available"));
  }


  private def loadInstalled() = {
    val file = new File(scbaz_dir, "installed") ;

    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath()) ;
      installed = InstalledList.fromXML(node) ;
    } else {
      installed = new InstalledList();
    }
  }
  loadInstalled();

  private def saveInstalled() = {
    val tmpFile = new File(scbaz_dir, "installed.tmp");
    val str = new FileWriter(tmpFile);
    str.write(installed.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(scbaz_dir, "installed"));
  }


  // load the universe specification from the directory 
  private def loadUniverse() = {
    val file = new File(scbaz_dir, "universe");
    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath());
      universe = Universe.fromXML(node);
    }
  }
  loadUniverse();

  private def saveUniverse() = {
    val tmpFile = new File(scbaz_dir, "universe.tmp");
    val str = new FileWriter(tmpFile);
    str.write(universe.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(scbaz_dir, "universe"));
  }


  // forget the notion of available files
  private def clearAvailable() = {
    available = PackageSet.Empty;
    (new File(scbaz_dir, "available")).delete();
  }


  def setUniverse(newUniverse : Universe) = {
    clearAvailable();

    universe = newUniverse;
    saveUniverse();
  }

  def install(pack : Package) = { 
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

    // extract a zip file in the specified directory
    def extractFiles(zip:ZipFile, directory:File) = {
      for(val ent <- mkList[ZipEntry](zip.entries())) {
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
      // XXX there should be a DependencyError
      throw new Error("package's dependencies are not yet installed");
    }

    if(! downloader.is_downloaded(pack.filename)) {
      downloader.download(pack.link, pack.filename)
    }

    val zip = new ZipFile(new File(downloader.dir, pack.filename)) ;

    val installedFiles = mkList[ZipEntry](zip.entries())
                         .map(ent => zipToFile(ent.getName()));


    installed.entryNamed(pack.name) match {
      case None => ();
      case Some(entry) => {
	// a same-named package is already present; 
	// mark it as broken and remove its files
	installed.add(entry.broken);
	saveInstalled();
	removeEntryFiles(entry);
      }
    }



    val newEntry = new InstalledEntry(pack.name,
				      pack.version,
				      installedFiles,
				      pack.depends,
				      false) ;

    installed.add(newEntry);
    saveInstalled();
    

    extractFiles(zip,directory);

    installed.add(newEntry.completed);
    saveInstalled();
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
      // XXX really, this should delete leading directory
      // entries, too, if they are empty....
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


object TestManagedDirectory {
  def main(args:Array[String]) : Unit = {
    val dir = new ManagedDirectory(new File("/home/lex/scala/scbaz/hacks/testcli"));

    Console.println(dir);

    dir.available.newestNamed("scalax") match {
      case None => {
	Console.println("no scalax package available!"); }
      case Some(pack) => {
	Console.println("installing " + pack);
	dir.install(pack);
      }
    }

    dir.available.newestNamed("scbaz") match {
      case Some(pack) => {
	Console.println("installing " + pack);
	dir.install(pack);
      }
    }

    // the following should cause an error, because scbaz depends on scalax
    dir.installed.entryNamed("scalax") match {
      case Some(ent) => {
	Console.println("removing " + ent);
	dir.remove(ent);
      }
    }
  }
}

