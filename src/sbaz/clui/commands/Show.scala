package sbaz.clui.commands

object Show extends Command {
  val name = "show"
  val oneLineHelp = "show information about one package"
  val fullHelp: String = (
    "show package [more packages ...]\n" +
    "\n" +
    "Show a summary of the specified packages.  Each package may\n" +
    "be specified either as either \"name\" or \"name/version\".  If\n" +
    "no version is specified, then the newest available version is\n" +
    "displayed.\n")


  def run(args: List[String], settings: Settings) = {
    import settings._

    if(args.isEmpty)
      usageExit

    for(val arg <- args) {
      val uspec = UserPackageSpecifierUtil.fromString(arg)
      uspec.chooseFrom(dir.available) match {
	case None =>
	  throw new Error("No available package matches " + arg)

	case Some(pack) => {
	  Console.println("Link: " + pack.link)
	  Console.println(pack.pack.longDescription)
	}

      }
    }
  }
}
