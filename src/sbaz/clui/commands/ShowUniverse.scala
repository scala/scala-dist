/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id: $

package sbaz.clui.commands

object ShowUniverse extends Command {
  val name = "showuniverse"
  val oneLineHelp = "show the active universe"

  val fullHelp =
    """showuniverse [ -x ]
    |
    |Show the active universe.  If -x is specified, give the
    |full XML description.
    |""".stripMargin

  def run(args: List[String], settings: Settings) = {
    import settings._

    args match {
      case Nil =>
        Console.println(dir.universe)
        
      case List("-x") =>
        Console.println(dir.universe.toXML)
        
      case _ =>
        usageExit
    }
  }
}
