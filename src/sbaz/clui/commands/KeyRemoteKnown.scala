package sbaz.clui.commands
import sbaz.keys._
import sbaz.{messages => msg}  
import scala.xml.XML
import java.io.StringReader

object KeyRemoteKnown extends Command {
  val name = "keyremoteknown"
  val oneLineHelp = "list all keys known to the bazaar server"
  val fullHelp: String = (
        "keyremoteknown\n" +
        "\n" +
        "List all keys known to the bazaar server.\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._
   
    chooseSimple.requestFromServer(msg.SendKeyList) match {
      case msg.NotOK(reason) => Console.println("error from server: " + reason)
      case msg.KeyList(keys) => {
       Console.println("Known keys:")
       for(val key <- keys)
         Console.println("  " + key)
      }
    }
  }
}
