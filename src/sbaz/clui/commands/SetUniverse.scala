/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import scala.xml.XML
import java.io.{FileReader, StringReader}

object SetUniverse extends Command {
  val name = "setuniverse"
  val oneLineHelp = "set the universe for a directory"
  val fullHelp = (
    "setuniverse filename\n" +
    "setuniverse -i descriptor\n" +
    "\n" +
    "Set the universe that will be used by the local managed directory.\n" +
    "The universe descriptor is either taken from the specified file,\n" +
    "or, if -i is specified, from the command line.  After setting\n" +
    "the universe, this command immediately runs an \"update\" so that\n" +
    "the list of available packages comes from the new universe.\n"
  )
  

  def run(args: List[String], settings: Settings) = {
    import settings._

    val usrc = args match {
      case List(fname) => new FileReader(fname)
      case List("-i", desc) => new StringReader(desc)
      case _ => usageExit
    }
    val unode = XML.load(usrc)
    val univ = Universe.fromXML(unode)

    if (!dryrun) {
      dir.setUniverse(univ)
      dir.updateAvailable
      Console.println("Universe established.")
    }
  }
}
