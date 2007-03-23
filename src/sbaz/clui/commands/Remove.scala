/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

object Remove extends Command {
  val name = "remove"
  val oneLineHelp = "remove a package"
  val fullHelp: String = (
    "remove package_name\n" +
    "\n" +
    "Remove (uninstall) the package with the specified name.\n")




  def run(args: List[String], settings: Settings) = {
    import settings._

    for (val name <- args) {
      dir.installed.entryNamed(name) match {
	case None =>
	  Console.println("no package named " + name)

	case Some(entry) =>
	  if (dir.installed.anyDependOn(entry.name)) {
	    val needers = dir.installed.entriesDependingOn(entry.name) 
	    val neednames = needers.map(.packageSpec) 

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
