package scbaz;

import java.net.URL ;
import java.io._ ;
import scala.xml._ ;

// A SimpleUniverse is a kind of universe that downloads packages
// only from one server.  The packages in the universe are then
// exactly the packages supplied by the one server.
class SimpleUniverse(name0:String, description0:String,
                     val hostname:String, val port:Int,
		     val packagesURL : URL)
extends Universe(name0,description0) {
  def retrieveAvailable() = {
    val connection = packagesURL.openConnection();
    val inputStream = connection.getInputStream();
    val outputStream = new ByteArrayOutputStream();

    def lp():Unit = {
      val dat = new Array[byte](1000);
      val numread = inputStream.read(dat);
      if(numread >= 0) {
	outputStream.write(dat,0,numread);
	lp();
      }
    }
    lp();
    
    // XXX this should consider character encodings more carefully....
    //     the local default is not a reliable choice.
    val packagesText = outputStream.toString();

    val xmlNode = XML.load(new StringReader(packagesText));
    val packageNodes = (xmlNode \ "package").toList ;

    val packages = packageNodes.map(node => Package.fromXML(node));

    new PackageSet(packages)
  }

  override def simpleUniverses = List(this) ;

  override def toString() = 
    "Universe \"" + name + "\" (" + hostname + ":" + port + ")";

  def toXML = 
    Elem(null, "simpleuniverse", Null, TopScope,
	 Elem(null, "name", Null, TopScope,
	      Text(name)),
	 Elem(null, "description", Null, TopScope,
	      Text(description)),
	 Elem(null, "hostname", Null, TopScope,
	      Text(hostname)),
	 Elem(null, "port", Null, TopScope,
	      Text(port.toString())),
	 Elem(null, "packagesURL", Null, TopScope,
	      Text(packagesURL.toString()))) ;
}


object SimpleUniverse {
  def fromXML(node:Node) = {
    val name = (node \ "name")(0).child(0).toString(false);
    val description = (node \ "description")(0).child(0).toString(false);
    val hostname = (node \ "hostname")(0).child(0).toString(false);
    val portString = (node \ "port")(0).child(0).toString(false);
    val urlString = (node \ "packagesURL")(0).child(0).toString(false);

    val port = Integer.decode(portString).intValue();
    val url = new URL(urlString);

    new SimpleUniverse(name, description, hostname, port, url);
  }
}


object TestSimpleUniverse {
  def main(args:Array[String]):Unit = {
    val univ = new SimpleUniverse("scala-dev",
				  "development universe of Scala",
				  "scalauniverses.dnsalias.net",
				  23256,
				  new URL("http://localhost/~lex/expacks/packages"));

    val xml = univ.toXML ;

    Console.println(univ);
    Console.println(xml);
    Console.println(Universe.fromXML(xml));
  }
}
