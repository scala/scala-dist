package sbaz.clui.commands

object Update extends Command {
  val name = "update"
  val oneLineHelp = "update the list of available packages"
  val fullHelp: String = "XXX"



  def run(args: List[String], settings: Settings) = {
    import settings._

    if(! args.isEmpty)
      usageExit

    if(! dryrun) {
      // XXX this should catch errors and report them gracefully
      dir.updateAvailable()
    }
  }
}
