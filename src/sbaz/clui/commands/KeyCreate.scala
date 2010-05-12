/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.clui.commands

import sbaz.clui._
import sbaz.keys._
import sbaz.{messages => msg}  
import scala.xml.XML
import java.io.StringReader

object KeyCreate extends Command {
  val name = "keycreate"
  val oneLineHelp = "request that a new key be created"
  val fullHelp: String = (
      "keycreate description message-pattern\n" +
      "\n" +
      "Create a key with the given description and message-pattern.\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._

    args match {
      case List(description, msgpattXML) => {
        val msgpatt = MessagePattern.fromXML(XML.load(new StringReader(msgpattXML)))
        chooseSimple.requestFromServer(msg.KeyCreate(msgpatt, description)) match {
          case msg.NotOK(reason) => Console.println("error returned: " + reason)
          case msg.KeyCreated(key) => {
            chooseSimple.addKey(key)
            Console.println("Created the following key: " + key)
          }
        }
      }
      
      case _ =>
        usageExit
    }
  }
}
