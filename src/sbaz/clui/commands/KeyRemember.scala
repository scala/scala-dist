package sbaz.clui.commands;
import sbaz.keys._
import scala.xml.XML
import java.io.StringReader

object KeyRemember extends Command {
  val name = "keyremember"
  val oneLineHelp = "remember the specified key for future use"
  val fullHelp: String = (
      "keyremember keyxml\n" +
      "\n" +
      "Remember the specified key for future use.  Future operations\n" +
      "that match the key will try to use that key.\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._

    args match {
      case List(keyXML) => {
        val key = KeyUtil.fromXML(XML.load(new StringReader(keyXML)))
        chooseSimple.addKey(key)
        Console.println("Key recorded.")
      }
      
      case _ => usageExit
    }    
  }
}
