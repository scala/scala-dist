/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import sbaz.clui._

object Help extends Command {
  val name = "help"
  val oneLineHelp = "display a help message"
  val fullHelp: String =
    ("sbaz help [ command ]\n" +
     "Display a help message.  If a command is specified, display\n" +
     "help for that option.  Otherwise, display a global help message.\n")


  def run(args: List[String], settings: Settings) {
    args match {
      case Nil =>
	Console.println("sbaz [ global_options... ] command command_options...")
	Console.println("")
	Console.println(settings.fullHelp)

	Console.println("")
	Console.println("Available commands:")
	Console.println("")
	for (cmd <- CommandUtil.allCommands) {
	  Console.println(cmd.name + " - " + cmd.oneLineHelp)
	}

      case List(cmdName) =>
	CommandUtil.named(cmdName) match {
	  case None => Console.println("No cammand named " + cmdName)
	  case Some(cmd) => Console.print(cmd.fullHelp)
	}

      case _ =>
        usageExit
    }
  }
}
