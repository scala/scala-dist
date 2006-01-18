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

  // Invalid arguments supplied.  These methods are called by run()
  // to print a usage message and then throw an Error exception
  def usageExit: All = {
    Console.println(fullHelp)
    throw new Error("invalid argument list")
  }

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
