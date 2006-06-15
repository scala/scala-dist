/* SBaz -- Scala Bazaar
 * Copyright 2005-2006 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui
import java.io.{File,FileInputStream}

// Global settings for the command-line UI
class Settings {

  val PRODUCT: String =
    System.getProperty("scala.tool.name", "sbaz")
  val VERSION: String =
    System.getProperty("scala.tool.version", "unknown version")
  val COPYRIGHT: String =
    System.getProperty("scala.copyright", "(c) 2005-2006 LAMP/EPFL")
  val versionMsg = PRODUCT + " " + VERSION + " -- " + COPYRIGHT

  // the name of the directory that is being managed
  var dirname = new File(Settings.home)

  // a ManagedDirectory opened on the same
  var dir: ManagedDirectory = null

  // whether to actually do the requested work, or to
  // just print out what would be done
  var dryrun = false

  // Whether to print out extra information about what
  // the tool is doing
  var verbose = false

  // The location of the miscellaneous helper files
  // needed by a ManagedDirectory.  Normally these
  // are taken from within the managed directory, but
  // developers of sbaz itself may wish to use different
  // versions.
  var miscdirname: File =
    { val str = System.getProperty("sbaz.miscdirhack")
      if (str == null)
        null
     else
       new File(str)
   }


  // XXX bogusly choose a simple universe to connect to
  def chooseSimple = {
    dir.universe.simpleUniverses.reverse(0)
  }

  // Parse global options from the beginning of a command-line.
  // Returns the portion of the command line that was not
  // consumed.
  def parseOptions(args: List[String]): List[String] =
    args match {
      case "-d" :: dirname :: rest =>
        this.dirname = new File(dirname)
        parseOptions(rest)

      case "-d" :: Nil =>
        //throw new Error("-d requires an argument")
        Console.println("Option -d requires an argument")
        exit(1)

      case ("-n" | "--dryrun") :: rest =>
        dryrun = true
        parseOptions(rest)

      case ("-v" | "--verbose") :: rest =>
        verbose = true
        parseOptions(rest)

      case "-version" :: rest =>
        Console.println(versionMsg)
        exit(0)

      case _ =>
        args
    }

  // describe the global options
  val fullHelp =
    "Global options:\n" +
    "\n" +
    "   -d <dir>        Operate on dir as the local managed directory.\n" +
    "   -n | --dryrun   Do not actually do anything.  Only print out what\n" +
    "                   tool would normally do with the following arguments.\n" +
    "   -v | --verbose  Output messages about what the sbaz tool is doing\n" +
    "   -version        Version information\n"
}


object Settings {

  val home = System.getProperty("scala.home", ".")

  // load system properties from scala.home/settings/sbaz.properties,
  // if that file is present.
  def loadSystemProperties: Unit = {
    val propFile = new File(new File(new File(home), "config"), "sbaz.properties")

    if (propFile.exists) {
      val reader = new FileInputStream(propFile)
      System.getProperties.load(reader)
      reader.close
    }
  }

}
