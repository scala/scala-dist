package sbaz;

import scala.xml._;
import scala.collection.mutable._;
import java.io._;
import sbaz.messages._ ;

// This class does the real processing with requests via the Servlet
// class.  For some reason, the servlets architecture bends over backwards
// to fight anyone who wants to have a single instance of their servlet.
// To avoid that fight, this class can be instantiated once per directory
// that is managed.

class ServletRequestHandler(directory:File) {
  val universe:SimpleUniverse =
    (Universe.fromXML(XML.load(new File(directory, "universe").getAbsolutePath())))
    match {
      case univ:SimpleUniverse => univ;
      case _ => throw new Error("this universe is not a simple universe");
    };

  val packagesFile = new File(directory, "packages");
  var packages =
    if(packagesFile.exists()) {
      AvailableListUtil.fromXML(XML.load(packagesFile.getAbsolutePath()));
    } else {
      new AvailableList(Nil);
    };

  def savePackages() = {
    val tmpFile = new File(directory, "packages");
    val str = new FileWriter(tmpFile);
    str.write(packages.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(directory, "packages"));
  }


  def responseForGET: String = synchronized {
    val out = new StringWriter();
    out.write("This is a Scala Bazaar.  The bazaar descriptor is:\n");
    out.write(universe.toXML.toString());
    out.write("\n\n");
    out.write("The packages included are:\n");
    for(val spec <- packages.sortedSpecs) {
      out.write(spec.toString());
      out.write("\n");
    }
    out.toString();
  }

  def handleRequest(req:Message): Message = synchronized {
    req match {
      case SendPackageList() => {
	LatestPackages(packages) ;
      }

      case AddPackage(pack) => {
	Console.println("adding new package: " + pack);
	val packsMinus = packages.available.filter(p => ! p.spec.equals(pack.spec));
	val newPacks = pack::packsMinus;
	packages = new AvailableList(newPacks);
	savePackages();
	OK();
      }
      
      case RemovePackage(spec) => {
	Console.println("removing package: " + spec);
	
	val packsMinus = packages.available.filter(p => ! p.spec.equals(spec));
	packages = new AvailableList(packsMinus);
	savePackages();
	OK();
      }

      case _ => NotOK("unhandled message type.  full message: " + req);
    }
  }

}


object ServletRequestHandler {
  // a map from canonical filenames to the handler for that directory
  private val handlers = new HashMap[String,ServletRequestHandler]();

  // Find the handler for a specified directory.  Create a new one if
  // necessary.
  def handlerFor(directory:File): ServletRequestHandler = synchronized {
    val fncanon = directory.getCanonicalPath();
    if(!handlers.contains(fncanon)) {
      val handler = new ServletRequestHandler(directory);
      handlers.update(fncanon, handler);
    }
    handlers(fncanon)
  }

  // convenience method for accessing the above
  def handlerFor(dirname:String): ServletRequestHandler = synchronized {
    handlerFor(new File(dirname)) 
  }
}
