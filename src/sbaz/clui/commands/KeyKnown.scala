/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import sbaz.keys._
import scala.xml.XML
import java.io.StringReader

object KeyKnown extends Command {
  val name = "keyknown"
  val oneLineHelp = "list all known keys"
  val fullHelp: String = (
      "keyknown [ -x ]\n" +
      "\n" +
      "List all known keys.  With -x, print the information in XML.\n")

  def run(args: List[String], settings: Settings) = {  
    import settings._
    var printXML = false
    
    args match {
      case Nil => ()
      case List("-x") => printXML = true
      case _ => usageExit 
    }
    
    val keys = chooseSimple.keys
    val sortedKeys = keys.sort((a,b) => a.toString < b.toString)

    if (printXML) {
      val keyring = new KeyRing(keys)
      Console.println(keyring.toXML)
    } else {
      if (keys.isEmpty)
        Console.println("No known keys for " + chooseSimple.name)
      else {
        Console.println("Known keys for " + chooseSimple.name + ":")
        for (val key <- sortedKeys)
          Console.println("  " + key)
      }
    }
  }
}
