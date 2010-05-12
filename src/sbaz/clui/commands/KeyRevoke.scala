/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.clui.commands

import sbaz.clui._
import sbaz.keys._
import sbaz.{messages => msg}  

object KeyRevoke extends Command {
  val name = "keyrevoke"
  val oneLineHelp = "request that a specified key be revoked"
  val fullHelp: String = (
      "keyrevoke keyfile\n" +
      "keyrevoke keyxml\n" +
      "\n" +
      "Tell the server to revoke the key described by keyfile or.\n" +
      "keyxml.  If the argument starts with <, it is assumed to\n" +
      "be XML, and is otherwise assumed to be a file containing the XML\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._

    args match {
      case List(keyspec) =>
        val key = KeyUtil.fromFileOrXML(keyspec)
        chooseSimple.requestFromServer(msg.KeyRevoke(key)) match {
          case msg.OK() => Console.println("OK, key revoked.")
          case msg.NotOK(reason) => Console.println("error returned: " + reason)
        }

      case _ =>
        usageExit
    }    
  }
}
