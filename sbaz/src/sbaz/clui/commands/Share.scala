/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.clui.commands

import java.io.{File, StringReader}
import java.net.URL
import org.xml.sax.SAXParseException

import scala.xml.XML
import sbaz._
import sbaz.clui._
import sbaz.messages._


object Share extends Command {
  val name = "share"
  val oneLineHelp = "upload a package description to the universe"
  val fullHelp: String =
    """share filename
    |share -i descriptor
    |share --template
    |
    |Share a package advertisement on a bazaar.  The package advertisement
    |is usually specified in a file, but it may also be specified on
    |the command line with the -i option.
    |
    |If --template is specified, then instead of uploading an advertisement,
    |the command prints out a template of a package advertisement.
    |""".stripMargin


  def run(args: List[String], settings: Settings): Unit = {
    import settings._

    val pack:AvailablePackage = args match {
      case List("--template") =>
        Console.println("<availablePackage>")
        Console.println("  <package>")
        Console.println("    <name></name>")
        Console.println("    <version></version>")
        Console.println("    <depends></depends>")
        Console.println("    <description></description>")
        Console.println("  </package>")
        Console.println("<link></link>")
        Console.println("</availablePackage>")
        null  // todo: return() here causes a compile error


      case List(fname)  =>
        try {
          AvailablePackageUtil.fromXML(XML.load(fname))
        } catch {
          case ex: SAXParseException =>
            if (new File(fname).exists()) {
              Console.println("Invalid XML for a package description.")
              Console.println("Expected is a valid .advert file.")
              exit(2)
            }
            else
              throw ex
        }

      case List("-i", arg) =>
        try {
          AvailablePackageUtil.fromXML(XML.load(new StringReader(arg)))
        } catch {
          case ex: FormatError =>
            if (new File(arg).exists()) {
              Console.println("Invalid XML for a package description.")
              Console.println("Did you mean to specify -f?")
              exit(2)
            } else {
              throw ex
            }
          case ex =>
            throw ex
        }
      
      case _ =>
        usageExit
    }

    if (pack == null) return

    // XXX this should do some sanity checks on the package:
    //  non-empty name, version, etc.
    //  name is only characters, numbers, dashes, etc.
    //  spec is not already included retract first if you want
    //    to replace something

    val univ = chooseSimple

    if (! dryrun) {
      univ.requestFromServer(AddPackage(pack))
      // XXX should check the reply

      // Immediately run an update, so that the user can see
      // their own newly shared package along with all
      // the other currently available packages.
      dir.updateAvailable()
    }

    Console.println("Package shared.")
  }
}
