package sbaz.clui.commands

object Installed extends Command {
  val name = "installed"
  val oneLineHelp = "list the packages that are installed"
  val fullHelp: String = (
    "installed\n" +
    "\n" +
    "Display the list of packages that are already installed.\n")



  def run(args: List[String], settings: Settings) = {
    import settings._

    if(! args.isEmpty)
      usageExit

    val sortedSpecs = dir.installed.sortedPackageSpecs 

    for(val spec <- sortedSpecs) {
      Console.println(spec)
    }

    Console.println(sortedSpecs.length.toString() + " packages installed")
  }
}
