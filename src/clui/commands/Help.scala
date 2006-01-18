package sbaz.clui.commands

object Help extends Command {
  val name = "help"
  val oneLineHelp = "display a help message"
  val fullHelp: String =
    ("sbaz help [ command ]\n" +
     "Display a help message.  If a command is specified, display\n" +
     "help for that option.  Otherwise, display a global help message.\n")


  def run(args: List[String], settings: Settings) = {
    // XXX first print out global options (deferred to Settings), then print out list of commands

    // XXX if a command name is specified, display help for IT....
    
    val buf = new StringBuffer()
       // XXX should get global options from Settings, and then print out explanation
    buf.append("sbaz [ -d directory ] [ -n ] command command_options...\n")

    for(val cmd <- CommandUtil.allCommands) {
      buf.append(cmd.name)
      buf.append(" - ")
      buf.append(cmd.oneLineHelp)
      buf.append("\n")
    }
    
    Console.print(buf.toString)
  }
}
