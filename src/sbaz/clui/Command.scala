/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui

import sbaz.clui.commands._

// A command type that the command-line UI can use
abstract class Command {
  // The name of the command, e.g. "update"
  val name: String

  // A one-line summary of the command's usage
  val oneLineHelp: String

  // A multi-line summary of the command's usage
  val fullHelp: String

  // Run the command with the given arguments.
  // It should throw exceptions if an error happens
  def run(args: List[String], settings: Settings): Unit

  /** Invalid arguments supplied.  Print a usage message and exit.
    */
  def usageExit: Nothing = {
    println(fullHelp)
    exit(1)
  }

  /** Invalid arguments supplied.  Print the explanation, print the
    * usage summary, and then exit.
    */
  def usageExit(explanation: String): Nothing = {
    println("invalid command: " + explanation)
    usageExit
  }
}

object CommandUtil {  // XXX naming it command causes a crash
  val allCommands =
    List(Available,
         Compact,
         Help,
         Installed,
         Install,
         KeyCreate,
         KeyForget,
         KeyKnown,
         KeyRemember,
         KeyRemoteKnown,
         KeyRevoke,
         Pack,
         Remove,
         Retract,
         SetUniverse,
         Setup,
         Share,
         Show,
         ShowUniverse,
         Update,
         Upgrade
    ).sort((a,b) => a.name <= b.name)

  def named(name: String): Option[Command] =
    allCommands.find(_.name == name)
}
