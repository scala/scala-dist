package sbaz.clui

import java.io.File
//import java.nio._ 
//import java.net._ 
//import java.nio.channels._ 
//import scala.xml.XML 
//import scala.collection.mutable.{HashSet, Queue} 


// A command line from the user.  This is the front end of the
// command-line interface to the Scala Bazaar system.
object CommandLine {
  Settings.loadSystemProperties

  val settings = new Settings()
  import settings._

  def errorExit(message: String):All = {
    Console.println("error: " + message)
    System.exit(2).asInstanceOf[All]
  }

  def usageExit():All = {
    commands.Help.run(List(), settings)
    System.exit(2) .asInstanceOf[All]
  }


  def processCommandLine(args:Array[String]):Unit = {
    // parse global options
    var argsleft = settings.parseOptions(args.toList)

    // extract the command name and command arguments
    val cmdName :: cmdArgs = argsleft match {
      case Nil => usageExit
      case a::b => argsleft
    }

    // set the miscdirname if it wasn't taken from
    // the environment
    if(miscdirname == null)
      miscdirname = new File(new File(dirname, "misc"),
			     "sbaz")

    // check if a new directory is being set up
    if(cmdName.equals("setup"))
      return commands.Setup.run(cmdArgs, settings)

    // if not, open an existing directory
    dir = new ManagedDirectory(dirname, miscdirname)
    
    // now find and run the requested command
    CommandUtil.named(cmdName) match {
      case None => usageExit()
      case Some(command) => {
	try {
	  command.run(cmdArgs, settings)
	} catch {
	  case er: Error => {
	    if(verbose)
	      throw er
	    else
	      Console.println(er.toString())
	  }
	}
      }
    }
  }

  def main(args:Array[String]): Unit = {
    processCommandLine(args)
  }
}
