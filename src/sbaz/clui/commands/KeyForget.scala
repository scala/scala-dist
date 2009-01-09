/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import sbaz.keys._

object KeyForget extends Command {
  val name = "keyforget"
  val oneLineHelp = "forget the specified key"
  val fullHelp: String = (
        "keyforget keyfile\n" +
        "keyforget keyxml\n" +
        "\n" +
        "Forget the specified key for future use.  Future operations\n" +
        "will stop trying to use that key.\n" +
        "\n" +
        "If the command argument starts with a '<' character, it\n" +
        "is assumed to be raw key data.  Otherwise, it is assumed to\n" +
        "be a file name.\n")

  def run(args: List[String], settings: Settings) {  
    import settings._

    args match {
      case List(keyspec) =>
        val key = KeyUtil.fromFileOrXML(keyspec)

        chooseSimple.forgetKey(key)
        Console.println("Key forgotten.")
	        
      case _ =>
        usageExit
    }
  }
}
