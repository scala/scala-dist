package scbaz;

import java.net.URL ;
import scala.xml._ ;

class SimpleUniverse(name0:String, description0:String,
                     val hostname:String, val port:Int,
		     val shortname:String,
		     val packages_url : URL)
extends Universe(name0,description0) {
  def retrieve_available_packages = { throw new Error("not yet xmplemented") }

  def toXML = Text("Not Yet Implemented")
}
