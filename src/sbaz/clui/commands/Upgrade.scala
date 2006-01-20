package sbaz.clui.commands
import scala.collection.mutable.{HashSet, Queue} 

object Upgrade extends Command {
  val name = "upgrade"
  val oneLineHelp = "upgrade all possible packages"
  val fullHelp: String = (
    "upgrade\n" +
    "\n" +
    "Upgrade all packages that can be upgraded to a newer version.\n")



  def run(args: List[String], settings: Settings) = {
    import settings._

    if(! args.isEmpty)
      usageExit

    // store both a set of specs in addition to the sequence of
    // packages to install, so as to improve performance
    val packsToInstall = new Queue[AvailablePackage]
    val specsToInstall = new HashSet[PackageSpec]

    for(val cur <- dir.installed.sortedPackageSpecs) {
      // the iteration is in sorted order so that
      // the behavior is deterministic

      dir.available.newestNamed(cur.name) match {
	case None =>
	  /* package stream is no longer in the universe ignore it */
	  ()
	case Some(newest) => {
	  if(! newest.spec.equals(cur)) {
	    try {
	      // try to upgrade from cur to newest
	      val allNeeded = dir.available.choosePackagesFor(newest.spec)
	      val newNeeded =
		allNeeded.toList
		  .filter(p => ! dir.installed.includes(p.spec))
		  .filter(p => ! specsToInstall.contains(p.spec))
	      
	      packsToInstall ++= newNeeded
	      specsToInstall ++= newNeeded.map(.spec)
	    } catch {
	      // its dependencies have a problem
	      // continue on trying to upgrade others
	      case _:DependencyError =>  {
		Console.println("Cannot upgrade to " + newest + " because of a failed dependency.")
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
	Console.println("Installing " + pack.spec + "...")
	if(! dryrun)
	  dir.install(pack)
      }
    }
  }
}
