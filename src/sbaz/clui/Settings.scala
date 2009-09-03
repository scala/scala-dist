/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui

import java.io.{File, FileInputStream}
import java.net.URL

import sbaz._

// Global settings for the command-line UI
class Settings {
  val SUS = SimpleUniverseSelectors

  val PRODUCT: String = "sbaz"
  val VERSION: String = sbaz.MyVersion.versionString
  val COPYRIGHT: String =
    System.getProperty("scala.copyright", "(c) 2005-2009 LAMP/EPFL")
  val versionMsg = PRODUCT + " " + VERSION + " -- " + COPYRIGHT

  // the name of the directory that is being managed
  var dirname = new File(Settings.home)

  // A ManagedDirectory opened on the same.
  // It is not opened until its first access.
  def dir: ManagedDirectory =
    dirCache match {
      case Some(dir) => dir
      case None =>
        dirCache = Some(new ManagedDirectory(dirname))
        dir
    }
 
  private var dirCache: Option[ManagedDirectory] = None

  // whether to actually do the requested work, or to
  // just print out what would be done
  var dryrun = false

  // Whether to print out extra information about what
  // the tool is doing
  var verbose = false

  /** The name of a universe to select from */

  // The location of the miscellaneous helper files
  // needed by a ManagedDirectory.  Normally these
  // are taken from within the managed directory, but
  // developers of sbaz itself may wish to use different
  // versions.
  var miscdirname: File = {
    val str = System.getProperty("sbaz.miscdirhack")
    if (str == null)
      null
    else
      new File(str)
  }

  /** The user-requested method for selecting a remote
   *  universe, for commands that need such a thing. */
  var simpleSelector: SUS.SimpleUniverseSelector = SUS.FirstKnown

  /** Choose a simple universe, based on the specified arguments.
   *  The available targetArgs are described in commands/Share.scala. */
  def chooseSimple: SimpleUniverse = {
    val knownSimples = dir.universe.simpleUniverses

    simpleSelector match {
      case SUS.FirstKnown =>
        if (knownSimples.isEmpty) {
	  println("I do not know which simple universe to use.")
	  println("Perhaps use --univ-url?")
	  exit(1)
	}
	knownSimples.head

      case SUS.WithName(name) =>
	knownSimples.find(_.name == name) match {
	  case Some(univ) => univ
	  case None =>
	    println("No universe found named: " + name)
	    println("Universes found: " +
		    knownSimples.map(_.name).mkString(", "))
	    exit(1)
	}

      case SUS.WithURL(url) =>
	new SimpleUniverse("(unnamed)", new URL(url))
    }
  }


  /** Parse global options from the beginning of a command-line.
   * Returns the portion of the command line that was not
   * consumed.
   */
  def parseOptions(args: List[String]): List[String] =
    args match {
      case "-d" :: dirname :: rest =>
        this.dirname = new File(dirname)
        parseOptions(rest)

      case "-d" :: Nil =>
        println("Option -d requires an argument")
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

      case "--univ" :: name :: rest =>
        simpleSelector = SUS.WithName(name)
        parseOptions(rest)

      case "--univ" :: Nil =>
        println("Option --univ requires an argument")
	exit(1)

      case "--univ-url" :: url :: rest =>
	simpleSelector = SUS.WithURL(url)
        parseOptions(rest)

      case "--univ-url" :: Nil =>
        println("Option --univ-url requires an argument")
	exit(1)

      case _ =>
        args
    }

  // describe the global options
  val fullHelp =
    """Global options:
      |
      |   -d <dir>        Operate on dir as the local managed directory.
      |   -n | --dryrun   Do not actually do anything.  Only print out what
      |                   tool would normally do with the following arguments.
      |   -v | --verbose  Output messages about what the sbaz tool is doing.
      |   -version        Version information.
      |   --univ name     Operate on the named remote universe.
      |   --univ-url url  Operate on the remote universe at the specified url.
      |""".stripMargin
}


object Settings {

  val home = System.getProperty("scala.home", ".")

  // load system properties from scala.home/config/sbaz.properties,
  // if that file is present.
  def loadSystemProperties {
    val propFile = new File(new File(new File(home), "config"), "sbaz.properties")

    if (propFile.isFile) {
      val reader = new FileInputStream(propFile)
      System.getProperties.load(reader)
      reader.close
    }
  }

}
