/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui

import java.io.{File, IOException}

// A command line from the user.  This is the front end of the
// command-line interface to the Scala Bazaar system.
object CommandLine {
  Settings.loadSystemProperties

  val settings = new Settings()
  import settings._

  def errorExit(message: String): Nothing = {
    Console.println("error: " + message)
    exit(2)
  }

  def usageExit(): Nothing = {
    commands.Help.run(List(), settings)
    exit(2)
  }

  def processCommandLine(args: Array[String]) {
    // parse global options
    var argsleft = settings.parseOptions(args.toList)

    // extract the command name and command arguments
    val cmdName :: cmdArgs = argsleft match {
      case Nil => usageExit
      case a::b => argsleft
    }

    // now find and run the requested command
    CommandUtil.named(cmdName) match {
      case None =>
        usageExit()
      case Some(command) =>
        try {
          command.run(cmdArgs, settings)
        } catch {
        case er: IOException =>
          if (verbose)
            throw er
          else {
            Console.println(er.toString())
            exit(1)
          }
        case er: Error =>
          if (verbose)
            throw er
          else {
            Console.println("Error: " + er.getMessage)
            exit(1)
          }
        }
    }
  }

  def main(args: Array[String]): Unit = {
    processCommandLine(args)
  }

}
