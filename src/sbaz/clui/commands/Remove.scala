/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

object Remove extends Command {
  val name = "remove"
  val oneLineHelp = "remove a package"
  val fullHelp: String =
    """remove package_name
    |
    |Remove (uninstall) the package with the specified name.
    |""".stripMargin

  def run(args: List[String], settings: Settings) = {
    import settings._

    for (val name <- args) {
      dir.installed.entryNamed(name) match {
	case None =>
	  Console.println("no package named " + name)

	case Some(entry) =>
	  if (dir.installed.anyDependOn(entry.name)) {
	    val needers = dir.installed.entriesDependingOn(entry.name) 
	    val neednames = needers.map(_.packageSpec) 

	    throw new Error(
                "package " + entry.packageSpec + 
                " is needed by: " +
                neednames.mkString("",",","") )
	  }

	  Console.println("removing " + entry.packageSpec)
	  if (! dryrun)
	    dir.remove(entry)
      }
    }
  }
}
