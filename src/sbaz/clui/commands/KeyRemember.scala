package sbaz.clui.commands;
import sbaz.keys._
import scala.xml.XML
import java.io.StringReader

object KeyRemember extends Command {
  val name = "keyremember"
  val oneLineHelp = "remember the specified key for future use"
  val fullHelp: String = (
      "keyremember keyfile\n" +
      "keyremember keyxml\n" +
      "\n" +
      "Remember the specified key for future use.  Future operations\n" +
      "that match the key will try to use that key.  If the argument\n" +
      "begins with a '<' character, it is assumed to be a key in\n" +
      "XML format.  Otherwise, it is assumed to be a file name.\n"
      )

  def run(args: List[String], settings: Settings) = {  
    import settings._

    args match {
      case List(keyspec) => {
				val key = KeyUtil.fromFileOrXML(keyspec)            
        chooseSimple.addKey(key)
        Console.println("Key recorded.")
      }
      
      case _ => usageExit
    }    
  }
}
