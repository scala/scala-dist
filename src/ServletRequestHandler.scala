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
      PackageSet.fromXML(XML.load(packagesFile.getAbsolutePath()));
    } else {
      PackageSet.Empty;
    };

  def savePackages() = {
    val tmpFile = new File(directory, "packages");
    val str = new FileWriter(tmpFile);
    str.write(packages.toXML.toString());
    str.close();
    tmpFile.renameTo(new File(directory, "packages"));
  }


  def handleRequest(req:Message): Message = {
    // XXX this should hold obtain a lock...

    req match {
      case SendPackageList() => {
	LatestPackages(packages) ;
      }

      case AddPackage(pack) => {
	Console.println("adding new package: " + pack);
	val packsMinus = packages.packages.filter(p => ! p.spec.equals(pack.spec));
	val newPacks = pack::packsMinus;
	packages = new PackageSet(newPacks);
	savePackages();
	OK();
      }
      
      case RemovePackage(spec) => {
	Console.println("removing package: " + spec);
	
	val packsMinus = packages.packages.filter(p => ! p.spec.equals(spec));
	packages = new PackageSet(packsMinus);
	savePackages();
	OK();  // XXX should return an error if the package is not present!
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
  def handlerFor(directory:File): ServletRequestHandler = {
    // XXX this should hold a lock...
    val fncanon = directory.getCanonicalPath();
    if(!handlers.contains(fncanon)) {
      val handler = new ServletRequestHandler(directory);
      handlers.update(fncanon, handler);
    }
    handlers(fncanon)
  }

  // convenience method for accessing the above
  def handlerFor(dirname:String): ServletRequestHandler =
    handlerFor(new File(dirname)) ;
}
