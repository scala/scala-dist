/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id: $

package sbaz.clui.commands

import scala.xml.XML
import java.io.{FileReader, StringReader}

object ShowUniverse extends Command {
  val name = "showuniverse"
  val oneLineHelp = "show the active universe"
  val fullHelp = (
    "showuniverse \n" +
    "\n" +
    "Show the active universe.\n"
  )

  def run(args: List[String], settings: Settings) = {
    import settings._

    args match {
      case Nil =>
        Console.println("Active: " + dir.universe)
      case _ =>
        usageExit
    }
  }
}
