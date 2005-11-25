package scbaz;

import java.net.URL ;
import java.io._ ;
import scala.xml._ ;
import scbaz.messages._ ;

// A SimpleUniverse is a kind of universe that downloads packages
// only from one server.  The packages in the universe are then
// exactly the packages supplied by the one server.
class SimpleUniverse(name0:String, description0:String,
		     val location: URL)
extends Universe(name0,description0) {
  override def retrieveAvailable(): PackageSet = {
    val response = requestFromServer(SendPackageList());
    response match {
      case LatestPackages(packs) => packs;
      case _ => throw new Error("unexpected response: " + response); // XXX choose a more specific exception
    }
  }

  override def simpleUniverses = List(this) ;

  // Make a low-level request to the server.
  // This method blocks until the server responds.
  // It throws an exception if there is a network problem.
  // XXX learn and document which exceptions are thrown
  def requestFromServer(request: Message): Message = {
    val connection = location.openConnection();
    connection.setDoOutput(true);
    val out = connection.getOutputStream();
    val bytesOut = request.toXML.toString().getBytes("UTF-8");
    out.write(bytesOut);
    out.close();

    val in = connection.getInputStream();
    val respBuf = new ByteArrayOutputStream();
    def lp():Unit = {
      val dat = new Array[byte](1000);
      val n = in.read(dat);
      if(n >= 0) {
	respBuf.write(dat,0,n);
	lp();
      }
    }
    lp();
   
    // XXX this should use whatever encoding the server specifified,
    // not hard code it to UTF-8
    val respString = respBuf.toString("UTF-8");
    MessageUtil.fromXML(XML.load(new StringReader(respString)));
  }

  override def toString() = 
    "Universe \"" + name + "\" (" + location + ")";

  def toXML = 
<simpleuniverse>
  <name>{name}</name>
  <description>
    {description}
  </description>
  <location>{location}</location>
</simpleuniverse> ;
}


// XXX naming it SimpleUniverse causes a compiler crash
object SimpleUniverseUtil {
  def fromXML(node:Node) = {
    val name = (node \ "name")(0).child(0).text;
    val description = (node \ "description")(0).child(0).text;
    val linkString = (node \ "location")(0).child(0).text;
    val link = new URL(linkString);

    new SimpleUniverse(name, description, link);
  }
}


object TestSimpleUniverse {
  def main(args:Array[String]):Unit = {
    val univ = new SimpleUniverse("scala-dev",
				  "development universe of Scala",
				  new URL("http://scalauniverses.dnsalias.net:23256/scala-dev"));

    val xml = univ.toXML ;

    Console.println(univ);
    Console.println(xml);
    Console.println(Universe.fromXML(xml));
  }
}
