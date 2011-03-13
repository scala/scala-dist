/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.clui.commands

import sbaz.clui._
import sbaz.keys._
import sbaz.{messages => msg}

object KeyRemoteKnown extends Command {
  val name = "keyremoteknown"
  val oneLineHelp = "list all keys known to the bazaar server"
  val fullHelp: String = (
        "keyremoteknown [ -x ]\n" +
        "\n" +
        "List all keys known to the bazaar server.  With -x, print\n"+
        "the keys in XML.\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._

    var printXML = false
    
    args match {
      case Nil => ()
      case List("-x") => printXML = true
      case _ => usageExit 
    }
   
    chooseSimple.requestFromServer(msg.SendKeyList) match {
      case msg.NotOK(reason) =>
        Console.println("error from server: " + reason)
      
      case msg.KeyList(keys) =>
        if (printXML) {
          val keyring = new KeyRing(keys)
          Console.println(keyring.toXML)
        } else {
          if(keys.isEmpty)
            Console.println("No known keys for " + chooseSimple.name)
          else {
            val sortedKeys = keys.sortWith((a,b) => a.toString < b.toString)
            Console.println("Known keys for " + chooseSimple.name + ":")
            for(key <- sortedKeys)
              Console.println("  " + key)
          }
        }
    }
  }
}
