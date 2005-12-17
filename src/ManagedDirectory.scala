package sbaz;

import java.io.{File, FileReader, FileWriter,
                FileOutputStream, BufferedOutputStream} ;
import java.net.URL;
import java.util.zip.{ZipFile,ZipEntry} ;
import java.util.jar.{Attributes, JarFile} ;
import scala.collection.immutable._ ;
import scala.xml._ ;


// ManagedDirectory manages one directory of installed packages.

// It enforces the dependencies between packages: There is never
// a set of installed packages where a fully installed package
// does not have its dependents installed.

class ManagedDirectory(val directory : File,
		       val miscdirectory : File)
{
  val meta_dir = new File(directory, "meta") ;
  val old_meta_dir = new File(directory, "scbaz") ;

  // check that the directory looks valid
  if(!meta_dir.isDirectory() && !old_meta_dir.isDirectory()) {
    throw new Error("Directory " + directory + 
                    " does not appear to be a sbaz-managed directory");
  };


  // if the directory has an scbaz subdir instead of meta,
  // change to the new name
  if(old_meta_dir.exists() && !meta_dir.exists()) {
    old_meta_dir.renameTo(meta_dir);
  }

  val downloader = new Downloader(new File(meta_dir, "cache")) ;

  // Load an XML doc from the specified filename.
  // The routine looks in meta_dir followed by old_meta_dir.
  private def loadXML[T](filename: String,
			 decoder: Node => T,
		         default: T)
                        : T =
  {
    val file = new File(meta_dir, filename);

    if(file.exists())
      decoder(XML.load(file.getAbsolutePath()))
    else
      default;
  }

  // Save an XML node to a file in the meta directory, being
  // careful to do it in a transactional style: first create
  // a tmp file, then rename the tmp file to the original.
  // If the underling renameTo() routine is atomic, then
  // at no time is the underlying file incomplete or missing.
  private def saveXML(xml: Node,
		      filename: String) =
  {
    val tmpFile = new File(meta_dir, filename + ".tmp");
    val str = new FileWriter(tmpFile);
    str.write(xml.toString());
    str.close();
    tmpFile.renameTo(new File(meta_dir, filename));
  }


  // Load the list of available packages
  var available: AvailableList = 
    loadXML("available",
	    AvailableListUtil.fromXML,
	    new AvailableList(Nil));

  private def saveAvailable() = {
    saveXML(available.toXML,
	    "available");
  }


  // Load the list of installed packages
  val installed: InstalledList  =  
    loadXML("installed",
	    InstalledList.fromXML,
	    new InstalledList());


  private def saveInstalled() = {
    saveXML(installed.toXML,
	    "installed");
  }


  // load the universe specification from the directory 
  var universe : Universe = 
    loadXML("universe",
	    Universe.fromXML,
	    new EmptyUniverse);

  private def saveUniverse() = {
    saveXML(universe.toXML,
	    "universe");
  }


  // forget the notion of available files
  private def clearAvailable() = {
    available = new AvailableList(Nil);
    (new File(meta_dir, "available")).delete();
  }


  def setUniverse(newUniverse : Universe) = {
    clearAvailable();

    universe = newUniverse;
    saveUniverse();
  }

  // parse a zip-ish "/"-delimited filename into a relative File
  private def zipToFile(name: String): File = {
    val path_parts = name.split("/").toList.filter(s => s.length() > 0) ;
    path_parts.foldLeft
               (new File(""))
               ((d,n) => new File(d,n)) ;
  }

  
  // Grab the main-class directive for a jar file, if present.
  // If not, return null.  The file should be absolute.
  private def mainClassOfJar(file: File): String = {
    val jar = new JarFile(file);
    val manifest = jar.getManifest();
    jar.close();

    if(manifest == null)
      return null;

    val attribs = manifest.getMainAttributes();
    val attribName = new Attributes.Name("Main-Class");

    return attribs.getValue(attribName);
  }


  // Guess whether a jar file is executable and thus should
  // get entries in the bin/ directory.  The function
  // assumes the file has been installed and thus can
  // be looked at.
  private def looksLikeExecutableJar(file: File): Boolean = {
    if(!file.getPath().matches("^[^/\\\\]*/?lib[/\\\\].*"))  // XXX big hack.  maybe the code should not use File's to remember installed entries...  perhaps a custom RelativePath class which simply has a list of strings?
      return false;
    if(!file.getName().endsWith(".jar"))
      return false;

    val mainClass = mainClassOfJar(new File(directory, file.getPath()));
    return(mainClass != null);
  }


  // Given an executable jar, pick a name for command-line bin/
  // entries that run the jar.
  private def commandNameForJar(file: File): String = {
    file.getName().replaceFirst("\\.jar$", "");
  }
			     

  // Try to make a file executable.  This routine runs chmod +x .
  // If chmod cannot be found, it fails quietly.
  private def makeExecutable(file: File) = {
    try {
      Runtime.getRuntime().exec(Array("chmod", "+x", file.getPath()))
    } catch {
      case _:java.io.IOException => ();
    }
  }

  // copy a file from one location to another, making
  // the specified susbtitutions along the way
  private def copyFileSubstituting(destFile: File,
				   sourceFile: File,
				   substs: List[Pair[String, String]]) =
  {
    // XXX make a readEntireFile method somewhere...
    val source = {
      val reader = new FileReader(sourceFile);
      val buf = new Array[char](sourceFile.length().asInstanceOf[int]);
      def readfrom(off: int): Unit = {
	val n = reader.read(buf, off, buf.length - off);
	if(n <= 0)
	  throw new Error("error reading from " + sourceFile);

	if(off + n < buf.length)
	  readfrom(off+n);
      }
      readfrom(0);

      reader.close();
      new String(buf);
    };
    

    val rewritten = substs.foldLeft(source)((str, subst) => {
      str.replaceAll(subst._1, subst._2);
    });


    // write to dest
    val writer = new FileWriter(destFile);
    writer.write(rewritten);
    writer.close();
  }
			       
  // Create entries in bin/ for the specified jar file.
  private def createAutoBinFiles(file: File): Unit = {
    val commandName = mainClassOfJar(new File(directory, file.getPath()));
    val substs = List(Pair("@jartorun@", file.getName()),
		      Pair("@mainclass@", commandName));

    for(val Pair(os, ext) <- List(Pair("unix", ""), Pair("mswin", ".bat"))) {
      val dest = new File(new File(directory, "bin"),
				  commandNameForJar(file) + ext);
      val src = new File(miscdirectory,
			 "smartrun." + os + ".template");
      copyFileSubstituting(dest, src, substs);
      makeExecutable(dest);
    }
  }

  // Create entries in bin/ for each of the specified
  // files that appears to be an auto-executable bin file.
  // Returns the list of relative File's that it created.
  private def createAutoBinFiles(files: List[File]): Unit = {
    for(val file <- files;
	looksLikeExecutableJar(file))
    {
      createAutoBinFiles(file);
    }
  }

  // Assuming the argument is an executable jar file,
  // remove the auto-created bin entries that were created
  // for it by createAutoBinFiles()
  private def removeAutoBinFiles(file: File) = {
    for(val ext <- List("", ".bat")) {
      val binfile =
	new File(new File(directory, "bin"),
		 commandNameForJar(file) + ext);
      binfile.delete();
    }
  }


  // install a package that has been downloaded
  def install(pack: Package, downloadedFile: File): Unit = {
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

	  if(ent.getName().startsWith("bin/"))
	    makeExecutable(file);
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
    createAutoBinFiles(installedFiles);
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
    for(val file <- entry.files;
	looksLikeExecutableJar(file))
    {
      removeAutoBinFiles(file);
    }


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
