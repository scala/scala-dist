package scbaz;

import java.io.{File, FileReader, FileWriter,
                FileOutputStream, BufferedOutputStream} ;
import scala.collection.immutable._ ;
import scala.xml._ ;
import java.util.zip.{ZipFile,ZipEntry} ;

class ManagedDirectory(directory : java.io.File) {
  val scbaz_dir = new File(directory, "scbaz") ;
  // XXX what if the scbaz directory doesn't exist?
  // option 1: have it automatically throw an error here
  // option 2: have a verify() method
  // option 3: have a ensureExists() method
  // multiple options possible; 2+3 looks reasonable

  var universe : Universe = new EmptyUniverse() ;

  var available : PackageSet = PackageSet.Empty ;
  var installed : InstalledList  =  new InstalledList() ;

  def loadAvailable() = {
    val file = new File(scbaz_dir, "available") ;

    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath()) ;
      available = PackageSet.fromXML(node) ;
    }
  }

  loadAvailable();


  def loadInstalled() = {
    val file = new File(scbaz_dir, "installed") ;

    if(file.exists()) {
      val node = XML.load(file.getAbsolutePath()) ;
      installed = InstalledList.fromXML(node) ;
    }
  }
  loadInstalled();

  def saveInstalled() = {
    val tmpFile = new File(scbaz_dir, "installed.tmp");
    val str = new FileWriter(tmpFile);
    str.write(installed.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(scbaz_dir, "installed"));
  }

  val downloader = new Downloader(new File(scbaz_dir, "cache")) ;



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

    // XXX should uninstall same-named package if there is one

    if(! downloader.is_downloaded(pack.filename)) {
      downloader.download(pack.link, pack.filename)
    }

    val zip = new ZipFile(new File(downloader.dir, pack.filename)) ;

    val installedFiles = mkList[ZipEntry](zip.entries())
                         .map(ent => zipToFile(ent.getName()));

    val newEntry = new InstalledEntry(pack.name, pack.version,
				      installedFiles,
				      false) ;

    installed.add(newEntry);
    saveInstalled();
    

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

    installed.add(newEntry.completed);
    saveInstalled();
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

    dir.available.newestNamed("scbaz") match {
      case None => {
	Console.println("no scbaz package available!"); }
      case Some(pack) => {
	Console.println("installing " + pack);
	dir.install(pack);
      }
    }
  }
}

