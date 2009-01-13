/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

object Update extends Command {
  val name = "update"
  val oneLineHelp = "update the list of available packages"
  val fullHelp: String =
    """update
    |
    |Update the list of available packages.
    |""".stripMargin

  def run(args: List[String], settings: Settings) = {
    import settings._

    if (! args.isEmpty)
      usageExit

    if (! dryrun)
      dir.updateAvailable()
    
    Console.println("Updated the list of available packages.")
  }
}
