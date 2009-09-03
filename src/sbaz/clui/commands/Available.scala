/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import scala.collection.immutable._
import sbaz._
import sbaz.clui._

object Available extends Command {
  val name = "available"
  val oneLineHelp = "list the available packages for installation"
  val fullHelp: String = (
    "available [ -a ]\n" +
    "\n" +
    "Display the list of packages that are available for installation.\n" +
    "If -a is specified, then print out all versions of each package instead\n" +
    "of just the most recent ones.\n")

  def run(args: List[String], settings: Settings) {
    import settings._

    var printall = false
    
    args match {
      case Nil => ()
      case List("-a") => printall=true
      case _ => usageExit
    }

    val specs = dir.available.packages.toList.map(_.spec)
		val nameSet = specs.foldLeft(new TreeSet[String])((set,spec) => set + spec.name)
    val names = nameSet.toList.sortWith((a,b) => a <= b)

    def versionsFor(name: String) = {
      val unsorted =
        for (spec <- specs if spec.name == name) yield spec.version
      unsorted.sortWith((v1,v2) => v1 >= v2)
    }
      
    def printVersions(specs: List[Version]) =
      print(specs.mkString("", ", ", ""))
      
    def printAllSpecs(name: String) {
      printVersions(versionsFor(name))
    }

    def printShortSpecs(name: String) {
      val toprint = 3
      val matching = versionsFor(name)
      if (matching.length <= toprint)
        printVersions(matching)
      else {
        printVersions(matching.take(toprint))
        Console.print(", ...")
      }
    }

    for (name <- names) {
      print(name + " (")
      if (printall)
        printAllSpecs(name)
      else
        printShortSpecs(name)
      Console.println(")")
    }
    println(names.length.toString + " package names")
    println(specs.length.toString + " total packages")
  }
}
