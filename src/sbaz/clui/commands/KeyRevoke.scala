package sbaz.clui.commands
import sbaz.keys._
import sbaz.{messages => msg}  
import scala.xml.XML
import java.io.StringReader

object KeyRevoke extends Command {
  val name = "keyrevoke"
  val oneLineHelp = "request that a specified key be revoked"
  val fullHelp: String = (
      "keyrevoke keyxml\n" +
      "\n" +
      "Tell the server to revoke the key described by keyxml")

  def run(args: List[String], settings: Settings) = {  
    import settings._

    args match {
      case List(keyXML) => {
        val key = KeyUtil.fromXML(XML.load(new StringReader(keyXML)))
        chooseSimple.requestFromServer(msg.KeyRevoke(key)) match {
          case msg.OK() => Console.println("OK, key revoked.")
          case msg.NotOK(reason) => Console.println("error returned: " + reason)
        }
      }
      
      case _ => usageExit
    }    
  }
}