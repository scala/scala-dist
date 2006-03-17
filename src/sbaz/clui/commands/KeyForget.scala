package sbaz.clui.commands;
import sbaz.keys._
import scala.xml.XML
import java.io.StringReader

object KeyForget extends Command {
  val name = "keyforget"
  val oneLineHelp = "forget the specified key"
  val fullHelp: String = (
        "keyforget keyxml\n" +
        "\n" +
        "Forget the specified key for future use.  Future operations\n" +
        "will stop trying to use that key.\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._

    args match {
    case List(keyXML) => {
      val key = KeyUtil.fromXML(XML.load(new StringReader(keyXML)))
      chooseSimple.forgetKey(key)
      Console.println("Key forgotten.")
    }
	        
    case _ => usageExit
    }    
  }
}
