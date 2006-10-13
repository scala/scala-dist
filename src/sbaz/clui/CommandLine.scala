/* SBaz -- Scala Bazaar
 * Copyright 2005-2006 LAMP/EPFL
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

  def processCommandLine(args: Array[String]): Unit = {
    // parse global options
    var argsleft = settings.parseOptions(args.toList)

    // extract the command name and command arguments
    val cmdName :: cmdArgs = argsleft match {
      case Nil => usageExit
      case a::b => argsleft
    }

    // check if a new directory is being set up
    if (cmdName.equals("setup"))
      return commands.Setup.run(cmdArgs, settings)

    // if not, open an existing directory
    dir = new ManagedDirectory(dirname)

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
          else
            Console.println(er.toString())
        case er: Error =>
          if (verbose)
            throw er
          else
            Console.println("Error: " + er.getMessage)
        }
    }
  }

  def main(args: Array[String]): Unit =
    processCommandLine(args)

}
