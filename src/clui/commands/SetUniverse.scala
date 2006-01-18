package sbaz.clui.commands
import scala.xml.XML
import java.io.StringReader

object SetUniverse extends Command {
  val name = "setuniverse"
  val oneLineHelp = "set the universe for a directory"
  val fullHelp = "XXX"
  

  def run(args: List[String], settings: Settings) = {
    import settings._

    if(args.length != 1)
      usageExit("setuniverse requires 1 argument: the universe description.")

    val unode = XML.load(new StringReader(args(0)))
    val univ = Universe.fromXML(unode)

    if(!dryrun) {
      dir.setUniverse(univ)
      dir.updateAvailable
      Console.println("Universe established.")
    }
  }
}
