/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import java.io.File
import ProposedChanges._

object Install extends Command {
  val name = "install"
  val oneLineHelp = "install a package"
  val fullHelp =
    """install package
    |
    |Install a package, including any of its necessary dependencies.
    |The package to install is specified in one of the following ways:
    |
    |    name         - Install the newest package with the specified name
    |    name/version - Install a package with a specified name and version
    |    -f filename  - Install the package located in the specified file
    |""".stripMargin

  def run(args: List[String], settings: Settings) = {
    import settings._

    args match {
      case List(arg) =>
        // install from the network

        val userSpec =
          try {
            UserPackageSpecifierUtil.fromString(arg)
          } catch {
            case e: FormatError =>
              val isFile = new File(arg).isFile()
              Console.println(
                if (isFile) "Use option '-f filename' to specify file names" else e.getMessage)
              exit(2)
          }

        val spec = userSpec.chooseFrom(dir.available) match {
          case None =>
            throw new Error("No available package matches " + arg + "!")
	
          case Some(pack) =>
            pack.spec
        }  

        val packages = 
          try {
            dir.available.choosePackagesFor(spec, dir.installed.packageNames) 
          } catch {
            case _:DependencyError =>
              // XXX not caught?
              // should explain the dependency problem....
              Console.println("Dependency error.")
              exit(2)
          }
	
        for (val pack <- packages)
          Console.println("planning to install: " + pack.spec)
  
        val additions = packages.toList.map(p => AdditionFromNet(p))
        val removals =
          for{val pack <- packages.toList
              val installedEntry <- dir.installed.entryNamed(pack.name).toList}
            yield Removal(installedEntry.packageSpec)
				val changes = removals ::: additions
        
        if (!dryrun) {
          Console.println("Installing...")
          dir.makeChanges(changes)
        }

      case List("-f", filename) =>
        // install directly from a file
        // XXX this should really try to grab the file's dependencies,
        // too, and/or print a helpful message if they cannot be found
	
        Console.println("Installing " + filename + "...")
        if (!dryrun) {
          dir.install(new File(filename))
        }

      case _ =>
        usageExit
    }
  }
}
