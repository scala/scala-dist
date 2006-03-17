package sbaz.clui
import sbaz.clui.commands._
import sbaz.keys._

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
  def usageExit: All = {
    Console.println(fullHelp)
    System.exit(1)
    throw new Error() // exit() returns Unit, not All...
  }

  /** Invalid arguments supplied.  Print the explanation, print the
    * usage summary, and then exit.
    */
  def usageExit(explanation: String): All = {
    Console.println("invalid command: " + explanation)
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
         Remove,
         Retract,
         SetUniverse,
         Setup,
         Share,
         Show,
         Update,
         Upgrade
    ).sort((a,b) => a.name <= b.name)
    
  def named(name: String): Option[Command] =
    allCommands.find(cmd => cmd.name == name)
}
