package scbaz;

import java.io._;
import scala.xml._;
import scala.collection.mutable.HashSet;
import java.nio.channels._;
import java.net.InetSocketAddress;
import scbaz.messages._ ;

// Serve a Universe out of a directory.
// INCOMPLETE.  This class is no longer used.  Instead, the 
// class Servlet is used via HTTP.
class Server(directory:File) {
  val universe:SimpleUniverse =
    (Universe.fromXML(XML.load(new File(directory, "universe").getAbsolutePath())))
    match {
      case univ:SimpleUniverse => univ;
      case _ => throw new Error("this universe is not a simple universe");
    };

  val port = universe.port;

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

    copyPackagesToWWW();  // XXX this should only happen periodically
  }

  // copy the packages over to a WWW directory, if one is present
  def copyPackagesToWWW() = {
    val wwwDir = new File(directory, "www");
    if(wwwDir.exists() && wwwDir.isDirectory()) {
      val inFile = new File(directory, "packages");
      val tmpFile = new File(wwwDir, "packages.tmp");
      
      val in = new FileInputStream(inFile);
      val out = new FileOutputStream(tmpFile);
      def lp():Unit = {
	val buf = new Array[byte](10000);
	val n = in.read(buf);
	if(n > 0) {
	  out.write(buf,0,n);
	  lp();
	}
      }
      lp();

      in.close();
      out.close();
      tmpFile.renameTo(new File(wwwDir, "packages"));
    }
  }


  def serve() = {
    val channel = ServerSocketChannel.open();
    channel.socket().bind(new InetSocketAddress(port));
    channel.configureBlocking(false);

    val connections = new HashSet[MessageStream]();

    def acceptNewConnections():Unit = {
      while(true) {
	val newChannel = channel.accept();
	if(newChannel == null)
	  return ();

	Console.println("new connection (" + connections.size + ")");

	newChannel.configureBlocking(false);
	connections.incl(new MessageStream(newChannel));
      }
    }

    def pruneDeadConnections() = {
      connections.filter(c => {
	if(! c.isConnected) Console.println("connection died");
	c.isConnected;
      });
    }
    
    def processConnections():Unit = {
      def processConnection(conn:MessageStream):Unit = {
	while(true) {
	  conn.receive() match {
	    case None => return ();
	    case Some(msg) =>
	      msg match {
		case AddPackage(pack) => {
		  Console.println("adding new package: " + pack);

		  val packsMinus = packages.packages.filter(p => ! p.spec.equals(pack.spec));
		  val newPacks = pack::packsMinus;
		  packages = new PackageSet(newPacks);
		  savePackages();
		}

		case RemovePackage(spec) => {
		  Console.println("removing package: " + spec);

		  val packsMinus = packages.packages.filter(p => ! p.spec.equals(spec));
		  packages = new PackageSet(packsMinus);
		  savePackages();
		}

		case _ => Console.println("received: " + msg);
	      }
	  }
	}
      }

      connections.toList.map(processConnection);
      ();
    }

    while(true) {
      acceptNewConnections();
      processConnections();
      pruneDeadConnections();

      Thread.sleep(10);
    }
  }
}



object Server2 {
  def main(args:Array[String]):Unit = {
    val dirname:String = args match {
      case Array() => "." ;
      case Array(d) => d;
      case _ => throw new Error("invalid command line")
    }

    val server = new Server(new File(dirname));
    server.serve();
  }
}
