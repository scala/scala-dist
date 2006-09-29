package sbaz.clui.commands
import messages._

object Retract extends Command {
  val name = "retract"
  val oneLineHelp = "retract a previously shared package"
  val fullHelp: String = (
    "retract name/version\n" +
    "\n" +
    "Retract a previously advertised package from the bazaar.\n" +
    "The package must be specified with both a name and a version.\n")



  def run(args: List[String], settings: Settings) = {
    import settings._

    args match {
      case List(rawspec) =>  {
        val spec =
 	  try {
            PackageSpecUtil.fromSlashNotation(rawspec)
 	  } catch{
 	    case ex:FormatError => {
 	      usageExit("Badly formed package specification: " + rawspec)
 	    }
 	  }
	    
        Console.println("removing " + spec + "...")
        if(! dryrun) {
          chooseSimple.requestFromServer(RemovePackage(spec)) match {
            case OK() =>  {
              Console.println("Package retracted.")

              // Immediately run an update, so that the user can see
              // a new state of the bazaar with the specified package
              // no longer present.
              dir.updateAvailable()
            }
      
            case resp => {
              Console.println("Unexpected response: " + resp)
            }
          }
        }
      }
      
      case _ => {
        Console.println("Specify a package name and version to retract from the server.")
        Console.println("For example: sbaz retract foo/1.3")
      }
    }
  }
}
