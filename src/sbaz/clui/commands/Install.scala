package sbaz.clui.commands
import java.io.File
import ProposedChanges._

object Install extends Command {
  val name = "install"
  val oneLineHelp = "install a package"
  val fullHelp = (
    "install package\n" +
    "\n" +
    "Install a package, including any of its necessary dependencies.  The\n" +
    "package to install is specified in one of the following ways:\n" +
    "\n" +
    "    name  -  Install the newest package with the specified name\n" +
    "    name/version - Install a package with a specified name and version\n" +
    "    -f filename - Install the package located in the specified file\n")

  def run(args: List[String], settings: Settings) = {
    import settings._

    args match {
      case List(arg) => {
        // install from the network

        val userSpec = UserPackageSpecifierUtil.fromString(arg)
        val spec = userSpec.chooseFrom(dir.available) match {
          case None =>
            throw new Error("No available package matches " + arg + "!")
	
          case Some(pack) =>
            pack.spec
        }  

        val packages = 
          try {
            dir.available.choosePackagesFor(spec) 
          } catch {
            case _:DependencyError => {
              // XXX not caught?
              // should explain the dependency problem....
              Console.println("Dependency error.")
              System.exit(2).asInstanceOf[All]
            }
          }
	
        for(val pack <- packages) {
          Console.println("planning to install: " + pack.spec)
        }
  
        val additions = packages.toList.map(p => AdditionFromNet(p))
        val removals =
          for{val pack <- packages.toList
              val installedEntry <- dir.installed.entryNamed(pack.name).toList}
            yield Removal(installedEntry.packageSpec)
				val changes = removals ::: additions
        
        if(!dryrun) {
          Console.println("Installing...")
          dir.makeChanges(changes)
        }
      }

      case List("-f", filename) => {
        // install directly from a file
        // XXX this should really try to grab the file's dependencies,
        // too, and/or print a helpful message if they cannot be found
	
        Console.println("Installing " + filename + "...")
        if(!dryrun) {
          dir.install(new File(filename))
        }
      }

      case _ => usageExit
    }
  }
}
