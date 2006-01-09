package sbaz;

import sbaz.messages._ ;
import java.io.{File,StringReader} ;
import java.nio._ ;
import java.net._ ;
import java.nio.channels._ ;
import scala.xml.XML ;
import scala.collection.mutable.{HashSet, Queue} ;


// A command line from the user.  This is the front end of the
// command-line interface to the Scala Bazaar system.
object CommandLine {
  // global options

  // the name of the directory that is being managed
  var dirname = new File(System.getProperty("scala.home", "."));

  // The location of the miscellaneous helper files
  // needed by a ManagedDirectory.  Normally these
  // are taken from within the managed directory, but
  // developers of sbaz itself may wish to use different
  // versions.
  var miscdirname: File =
    { val str = System.getProperty("sbaz.miscdirhack");
      if(str == null)
	null;
     else
       new File(str);
   };

  // a ManagedDirectory opened on the same
  var dir:ManagedDirectory = null ;

  // whether to actually do the requested work, or to
  // just print out what would be done
  var dryrun = false;
  

  def error_exit(message: String):All = {
    Console.println("error: " + message);
    java.lang.System.exit(2).asInstanceOf[All];
  }

  def print_usage() = {
    Console.println("sbaz [ -d directory ] [ -n ] command command_options...");
    Console.println("setup - initialize a directory to be managed");
    Console.println("setuniverse - set the universe for a directory");
    Console.println("installed - list the packages that are installed");
    Console.println("available - list the available packages for installation");
    Console.println("show - show information about one package");
    Console.println("install - install a package");
    Console.println("remove - remove a package");

    Console.println("update - update the list of available packages");
    Console.println("upgrade - upgrade all packages that can be");
    Console.println("compact - clear the download cache to save space");

    Console.println("share - upload a package description to the universe");
    Console.println("retract - retract a previously uploaded package");
  }

  def usage_exit():All = {
    print_usage();
    java.lang.System.exit(2) .asInstanceOf[All];
  }

  def setup(args:List[String]) = {
    if(args.length > 0)
      usage_exit();

    //XXX all this setup code should be moved to a ManagedDirectory object...
    val meta_dirname = new File(dirname, "meta");

    if(meta_dirname.exists())
      error_exit("the directory " + dirname + " looks like it is already set up");

    
    meta_dirname.mkdirs();
    // XXX it would be nice to make the sbaz directory non-readable
    //     by anyone but the user....
  }


  def setuniverse(args:List[String]) = {
    if(args.length != 1)
      error_exit("setuniverse requires 1 argument: the universe description.");

    val unode = XML.load(new StringReader(args(0)));
    val univ = Universe.fromXML(unode);

    if(!dryrun) {
      dir.setUniverse(univ);
     
      Console.println("Universe established.  You should probably run \"sbaz update\".");
    }
  }

  def install(args:List[String]) = {
    args match {
      case List(arg) => {
	// install from the network

	val userSpec = UserPackageSpecifierUtil.fromString(arg);
	val spec = userSpec.chooseFrom(dir.available) match {
	  case None =>
	    throw new Error("No available package matches " + arg + "!");

	  case Some(pack) =>
	    pack.spec
	};

	val packages = 
	  try {
	    dir.available.choosePackagesFor(spec) ;
	  } catch {
	    case _:DependencyError => {
	      // XXX not caught?
	      Console.println("Dependency error.");
	      System.exit(2).asInstanceOf[All];
	    }
	    case ex => throw ex;
	  };
	
	for(val pack <- packages) {
	  if(! dir.installed.includes(pack.spec)) {
	    Console.println("installing " + pack.spec);
	    
	    if(! dryrun) {
	      dir.install(pack);
	    }
	  }
	}
      };

      case List("-f", filename) => {
	// install directly from a file
	// XXX this should really try to grab the file's dependencies,
	// too...

	dir.install(new File(filename));
      }

      case _ => usage_exit();
    }
  }

  def remove(args:List[String]) = {
    for(val name <- args) {
      dir.installed.entryNamed(name) match {
	case None => {
	  Console.println("no package named " + name);
	} ;
	case Some(entry) => {
	  if(dir.installed.anyDependOn(entry.name)) {
	    val needers = dir.installed.entriesDependingOn(entry.name) ;
	    val neednames = needers.map(.name) ;

	    // XXX the below has an ugly List() in it
	    error_exit("package " + entry + " is needed by " + neednames) ;
	  }

	  Console.println("removing " + entry.packageSpec);
	  if(! dryrun)
	    dir.remove(entry);
	}
      }
    }
  }

  def upgrade(args: List[String]) = {
    if(! args.isEmpty)
      usage_exit();

    // store both a set of specs in addition to the sequence of
    // packages to install, so as to improve performance
    val packsToInstall = new Queue[AvailablePackage];
    val specsToInstall = new HashSet[PackageSpec];

    for(val cur <- dir.installed.sortedPackageSpecs) {
      // the iteration is in sorted order so that
      // the behavior is deterministic

      dir.available.newestNamed(cur.name) match {
	case None =>
	  /* package stream is no longer in the universe; ignore it */
	  ();
	case Some(newest) => {
	  if(! newest.spec.equals(cur)) {
	    try {
	      // try to upgrade from cur to newest
	      val allNeeded = dir.available.choosePackagesFor(newest.spec);
	      val newNeeded =
		allNeeded.toList
		  .filter(p => ! dir.installed.includes(p.spec))
		  .filter(p => ! specsToInstall.contains(p.spec));
	      
	      packsToInstall ++= newNeeded;
	      specsToInstall ++= newNeeded.map(.spec);
	    } catch {
	      // its dependencies have a problem;
	      // continue on trying to upgrade others
	      case _:DependencyError =>  {
		Console.println("Cannot upgrade to " + newest + " because of a failed dependency.");
	      }
	    }
	  }
	}
      }
    }

    if(packsToInstall.isEmpty)
      Console.println("Nothing to upgrade.")
    else {
      for(val pack <- packsToInstall) {
	Console.println("Installing " + pack.spec + "...");
	if(! dryrun)
	  dir.install(pack);
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

  def show(args: List[String]) = {
    if(args.isEmpty)
      usage_exit();

    for(val arg <- args) {
      val uspec = UserPackageSpecifierUtil.fromString(arg);
      uspec.chooseFrom(dir.available) match {
	case None =>
	  throw new Error("No available package matches " + arg);

	case Some(pack) => {
	  Console.println("Link: " + pack.link);
	  Console.println(pack.pack.longDescription)
	};

      }
    }
  }

  def update(args:List[String]) = {
    if(! args.isEmpty)
      usage_exit();

    if(! dryrun) {
      // XXX this should catch errors and report them gracefully
      dir.updateAvailable();
    }
  }


  // XXX bogusly choose a simple universe to connect to
  private def chooseSimple = {
    dir.universe.simpleUniverses.reverse(0)
  }

  // add a package
  def share(args:List[String]):Unit = {
    val pack = args match {
      case List("--template") => {
	Console.println("<availablePackage>");
	Console.println("  <package>");
	Console.println("    <name></name>");
	Console.println("    <version></version>");
	Console.println("    <depends></depends>");
	Console.println("    <description></description>");
	Console.println("  </package>");
	Console.println("<link></link>");
	Console.println("</availablePackage>");
	null;
      }


      case List("-f", fname) =>  // COMPAT.  remove before long...
	AvailablePackageUtil.fromXML(XML.load(fname));

      case List(fname)  =>
	AvailablePackageUtil.fromXML(XML.load(fname));
      
      case List("-i", arg) =>
	try {
	  AvailablePackageUtil.fromXML(XML.load(new StringReader(arg)));
	} catch {
	  case ex:FormatError => {
	    if(new File(arg).exists()) {
	      Console.println("Invalid XML for a package description.");
	      Console.println("Did you mean to specify -f?");
	      System.exit(2).asInstanceOf[All];
	    } else {
	      throw ex;
	    }
	  }
	  case ex => throw ex;
	};
      
      case _ => usage_exit();  // XXX need usage for add
    }

    if(pack == null)
      return();

    // XXX this should do some sanity checks on the package:
    //  non-empty name, version, etc.
    //  name is only characters, numbers, dashes, etc.
    //  spec is not already included; retract first if you want
    //    to replace something

    if(! dryrun) {
      chooseSimple.requestFromServer(AddPackage(pack));
      // XXX should check the reply

      // Immediately run an update, so that the user can see
      // their own newly shared package along with all
      // the other currently available packages.
      dir.updateAvailable();
    }

    Console.println("Package shared.");
  }

  // remove a package from the bazaar
  def retract(args:List[String]):Unit = {
    args match {
      case List(rawspec) =>  {
 	val spec =
// XXX the following triggers a compiler bug; the Java verifier fails
// 	  try {
 	    PackageSpecUtil.fromSlashNotation(rawspec);
// 	  } catch{
// 	    case ex:FormatError => {
// 	      error_exit("Badly formed package specification: " + rawspec);
// 	    }
// 	    case ex@_ => throw ex;
// 	  };
	    
 	Console.println("removing " + spec + "...");
 	if(! dryrun) {
 	  chooseSimple.requestFromServer(RemovePackage(spec));
 	  // XXX should check the reply

	  // Immediately run an update, so that the user can see
          // a new state of the bazaar with the specified package
          // no longer present.
	  dir.updateAvailable();
 	}
      }
      case _ => {
	Console.println("Specify a package name and version to retract from the server.");
	Console.println("For example: sbaz retract foo/1.3");
      }
    }
  }

  def processCommandLine(args:Array[String]):Unit = {
    var argsleft = args.toList ;

    while(true) {
      argsleft match {
	case Nil =>
	  usage_exit();
	case arg :: rest => {
	  argsleft = rest ;
// XXX match on argsleft ?
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

	      // set the miscdirname if it wasn't taken from
	      // the environment
	      if(miscdirname == null)
		miscdirname = new File(new File(dirname, "misc"),
				       "sbaz");

	      // check if a new directory is being
	      // set up.
	      if(arg.equals("setup"))
		return setup(rest);

	      // if not, open an existing directory
	      dir = new ManagedDirectory(dirname, miscdirname);

	      arg match {
		case "setuniverse" => return setuniverse(rest);
		case "install" => return install(rest);
		case "remove" => return remove(rest);
		case "installed" => return installed(rest);
		case "available" => return available(rest);
		case "show" => return show(rest);
		case "update" => return update(rest);
		case "upgrade" => return upgrade(rest);

		case "share" => return share(rest);
		case "retract" => return retract(rest);

		case _ => usage_exit();
	      }
	    }
	  }
	}
      }
    }
  }

  def main(args:Array[String]) = {
    try {
      processCommandLine(args);
    } catch {
      case ex:Error => {
	Console.println("Error: " + ex);
      }
    }
  }
}
