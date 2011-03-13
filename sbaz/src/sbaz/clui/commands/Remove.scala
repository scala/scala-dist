/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.clui.commands

import sbaz._
import sbaz.clui._
import sbaz.ProposedChanges._

object Remove extends Command {
  val name = "remove"
  val oneLineHelp = "remove packages"
  val fullHelp: String =
    """remove package_name...
    |
    |Remove (uninstall) the package(s) with the specified name(s).
    |""".stripMargin

  def run(args: List[String], settings: Settings) = {
    import settings._

    val entries = args.foldLeft[List[InstalledEntry]](List.empty) { (list, arg) =>
      val entry = dir.installed.entryNamed(arg).orElse { 
        Console.println("No installed package named '" + arg + "'")
        None
      }
      list ::: entry.toList
    }
    
    if(entries.length == args.length) {
      val changes = entries.map(entry => {
        Console.println("removing " + entry.packageSpec)
        Removal(entry.packageSpec)
      })
      if (! dryrun) dir.makeChanges(changes)
    } else {
      throw new Error("Removal aborted.")
    }
  }
}
