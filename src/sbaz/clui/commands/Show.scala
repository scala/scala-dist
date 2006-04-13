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
    "displayed.  If there is a package already installed with the requested\n" +
    "specification, then that package is shown in preference to a matching\n" +
    "package from the bazaar.\n")


  def run(args: List[String], settings: Settings) = {
    import settings._

    if(args.isEmpty)
      usageExit

    for(val arg <- args) {
      val uspec = UserPackageSpecifierUtil.fromString(arg)
      
			uspec.chooseFrom(dir.installed) match {
        case Some(pack) =>
          Console.println(pack.pack.longDescription)
					Console.println("Files included:")
          for(val file <-pack.files)
            Console.println("  " + file)
          
        case None =>
          uspec.chooseFrom(dir.available) match {
            case Some(pack) =>
              Console.println(pack.pack.longDescription)
              Console.println("Link: " + pack.link)
            case None =>
              Console.println("No available package matches " + arg)
          }
      }
    }
  }
}
