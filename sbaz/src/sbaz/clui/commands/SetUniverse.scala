/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.clui.commands

import scala.xml.XML
import java.io.{FileReader, StringReader, File}
import sbaz._
import sbaz.clui._

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

    val univ = args match {
      case List(fname) => 
        val file = new File(fname)
        if (!file.exists) {
          println(file.toString + " does not exist")
          exit(1)
        }
        Universe.fromFile(file)

      case List("-i", desc) =>
        Universe.fromString(desc)

      case _ => usageExit
    }

    if (!dryrun) {
      dir.setUniverse(univ)
      dir.updateAvailable
      println("Universe established.")
    }
  }
}
