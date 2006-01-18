package sbaz.clui.commands
import java.io.File

object Setup extends Command {
  val name = "setup"
  val oneLineHelp = "initialize a directory to be managed"
  val fullHelp: String = "XXX"



  def run(args: List[String], settings: Settings) = {
    import settings._

    if(args.length > 0)
      usageExit

    //XXX all this setup code should be moved to a ManagedDirectory object...
    val meta_dirname = new File(dirname, "meta")

    if(meta_dirname.exists())
      throw new Error(
	"the directory " + dirname + " looks like it is already set up")

    if(! dryrun) {
      meta_dirname.mkdirs()
      // XXX it would be nice to make the sbaz directory non-readable
      //     by anyone but the user....
    }
  }
}
