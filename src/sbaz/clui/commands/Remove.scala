package sbaz.clui.commands

object Remove extends Command {
  val name = "remove"
  val oneLineHelp = "remove a package"
  val fullHelp: String = (
    "remove package_name\n" +
    "\n" +
    "Remove (uninstall) the package with the specified name\n")




  def run(args: List[String], settings: Settings) = {
    import settings._

    for(val name <- args) {
      dir.installed.entryNamed(name) match {
	case None => {
	  Console.println("no package named " + name)
	} 
	case Some(entry) => {
	  if(dir.installed.anyDependOn(entry.name)) {
	    val needers = dir.installed.entriesDependingOn(entry.name) 
	    val neednames = needers.map(.name) 

	    // XXX the below has an ugly List() in it
	    throw new Error("package " + entry + " is needed by " + neednames) 
	  }

	  Console.println("removing " + entry.packageSpec)
	  if(! dryrun)
	    dir.remove(entry)
	}
      }
    }
  }
}
