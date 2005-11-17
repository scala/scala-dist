package scbaz;

import scala.xml._ ;

abstract class Universe(val name:String, val description:String) {
  def toXML : Node ;
  
  def retrieve_available_packages : PackageSet ;
  

//  abstract val auto_update_url : URL ;
}


object Universe {
  // def fromXML(xml:Node) = ...  XXX
}
