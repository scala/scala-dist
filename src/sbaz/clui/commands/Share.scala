package sbaz.clui.commands
import messages._
import scala.xml.XML
import java.io.{File, StringReader}

object Share extends Command {
  val name = "share"
  val oneLineHelp = "upload a package description to the universe"
  val fullHelp: String = (
    "share filename\n" +
    "share -i descriptor\n" +
    "share --template\n" +
    "\n" +
    "Share a package advertisement on a bazaar.  The package advertisement\n" +
    "is usually specified in a file, but it may also be specified on\n" +
    "the command line with the -i option.\n" +
    "\n" +
    "If --template is specified, then instead of uploading a description,\n" +
    "the command prints out a template of a package advertisement.\n")


  def run(args: List[String], settings: Settings): Unit = {
    import settings._

    val pack = args match {
      case List("--template") => {
	Console.println("<availablePackage>")
	Console.println("  <package>")
	Console.println("    <name></name>")
	Console.println("    <version></version>")
	Console.println("    <depends></depends>")
	Console.println("    <description></description>")
	Console.println("  </package>")
	Console.println("<link></link>")
	Console.println("</availablePackage>")
	null  // return() here causes a compile error
      }


      case List("-f", fname) =>  // COMPAT.  remove before long...
	AvailablePackageUtil.fromXML(XML.load(fname))

      case List(fname)  =>
	AvailablePackageUtil.fromXML(XML.load(fname))
      
      case List("-i", arg) =>
	try {
	  AvailablePackageUtil.fromXML(XML.load(new StringReader(arg)))
	} catch {
	  case ex:FormatError => {
	    if(new File(arg).exists()) {
	      Console.println("Invalid XML for a package description.")
	      Console.println("Did you mean to specify -f?")
	      exit(2)
	    } else {
	      throw ex
	    }
	  }
	  case ex => throw ex
	}
      
      case _ => usageExit
    }

    if(pack == null)
      return()



    // XXX this should do some sanity checks on the package:
    //  non-empty name, version, etc.
    //  name is only characters, numbers, dashes, etc.
    //  spec is not already included retract first if you want
    //    to replace something

    if(! dryrun) {
      chooseSimple.requestFromServer(AddPackage(pack))
      // XXX should check the reply

      // Immediately run an update, so that the user can see
      // their own newly shared package along with all
      // the other currently available packages.
      dir.updateAvailable()
    }

    Console.println("Package shared.")
  }
}
