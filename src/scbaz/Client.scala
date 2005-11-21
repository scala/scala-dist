package scbaz;

import java.io.{File,StringReader} ;
import scala.xml.XML ;

object Client {
  // global options

  // the directory that is being managed
  var dirname = new File(".");
  var dir:ManagedDirectory = null ;

  // whether to actually do the requested work, or to
  // just print out what would be done
  var dryrun = false;
  

  def error_exit(message: String):Any = {
    Console.println("error: " + message);
    java.lang.System.exit(2);
  }

  def print_usage() = {
    Console.println("scbaz [ -d directory ] [ -n ] command command_options...");
    Console.println("setup - initialize a directory to be managed");
    Console.println("setuniverse - set the universe for a directory");
    Console.println("install - install a package");
    Console.println("remove - remove a package");
    Console.println("update - update the list of available packages");
    Console.println("upgrade - upgrade all packages that can be");
    Console.println("installed - list the packages that are installed");
    Console.println("available - list the available packages for installation");
    Console.println("compact - clear the download cache to save space");
  }

  def usage_exit():Any = {
    print_usage();
    java.lang.System.exit(2);
  }

  def setup(args:List[String]) = {
    if(args.length > 0)
      usage_exit();

    val scbaz_dirname = new File(dirname, "scbaz");

    if(scbaz_dirname.exists())
      error_exit("the directory " + dirname + " looks like it is already set up");

    
    scbaz_dirname.mkdirs();
    // XXX it would be nice to make the scbaz directory non-readable
    //     by anyone but the user....
  }


  def setuniverse(args:List[String]) = {
    if(args.length != 1)
      error_exit("setuniverse requires 1 argument: the universe description.");

    val unod = XML.load(new StringReader(args(0)));
    val univ = Universe.fromXML(unod);
    dir.setUniverse(univ);

    Console.println("Universe established.  You should probably run \"scbaz update\".");
  }

  // XXX needs to respect -n
  def install(args:List[String]) = {
    for(val name <- args) {
      val packages = dir.available.choosePackagesFor(name) ;

      for(val pack <- packages) {
	if(! dir.installed.includes(pack.spec)) {
	  Console.println("installing " + pack.spec);
	  dir.install(pack);
	}
      }
    }
  }

  // XXX needs to respect -n
  def remove(args:List[String]) = {
    for(val name <- args) {
      dir.installed.entryNamed(name) match {
	case None => () ;
	case Some(entry) => {
	  if(dir.installed.anyDependOn(entry.name)) {
	    val needers = dir.installed.entriesDependingOn(entry.name) ;
	    val neednames = needers.map(.name) ;

	    // XXX the below has an ugly List() in it
	    error_exit("package " + entry + " is needed by " + neednames) ;
	  }

	  Console.println("removing " + entry.packageSpec);
	  dir.remove(entry);
	}
      }
    }
  }

  def installed(args:List[String]) = {
    if(! args.isEmpty)
      usage_exit();

    val sortedSpecs = dir.installed.sortedPackageSpecs ;

    for(val spec <- sortedSpecs) {
      Console.println(spec);
    }
    Console.println(sortedSpecs.length.toString() + " packages installed")
  }

  def available(args:List[String]) = {
    if(! args.isEmpty)
      usage_exit();

    val sortedSpecs = dir.available.sortedSpecs ;

    for(val spec <- sortedSpecs) {
      Console.println(spec);
    }
    Console.println(sortedSpecs.length.toString() + " packages available")
  }

  def update(args:List[String]) = {
    if(! args.isEmpty)
      usage_exit();

    // XXX this should catch errors and report them gracefully
    dir.updateAvailable();
  }

  def processCommandLine(args:Array[String]):Unit = {
    var argsleft = args.toList ;

    while(true) {
      argsleft match {
	case Nil =>
	  usage_exit();
	case arg :: rest => {
	  argsleft = rest ;

	  arg match {
	    case "-n" => {
	      dryrun = true;
	    }
	    case "-d" => {
	      argsleft match {
		case Nil => usage_exit();
		case arg :: rest => {
		  argsleft = rest;
		  dirname = new File(arg);
		}
	      }
	    }

	    case _ => {
	      // not a global option; the command has been reached

	      dir = new ManagedDirectory(dirname);

	      arg match {
		case "setup" => return setup(rest);
		case "setuniverse" => return setuniverse(rest);
		case "install" => return install(rest);
		case "remove" => return remove(rest);
		case "installed" => return installed(rest);
		case "available" => return available(rest);
		case "update" => return update(rest);

		case _ => usage_exit();
	      }
	    }
	  }
	}
      }
    }
  }

  def main(args:Array[String]) = {
    this.processCommandLine(args);
  }
}
