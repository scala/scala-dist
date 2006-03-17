package sbaz.clui.commands;
import sbaz.keys._
import scala.xml.XML
import java.io.StringReader

object KeyKnown extends Command {
  val name = "keyknown"
  val oneLineHelp = "list all known keys"
  val fullHelp: String = (
      "keyknown\n" +
      "\n" +
      "List all known keys.\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._
    
    val keys = chooseSimple.keys
    val sortedKeys = keys.sort((a,b) => a.toString < b.toString)
    
    if(keys.isEmpty)
      Console.println("No known keys for " + chooseSimple.name)
    else {
      Console.println("Known keys for " + chooseSimple.name + ":")
      for(val key <- sortedKeys)
        Console.println("  " + key)
    }
  }
}
