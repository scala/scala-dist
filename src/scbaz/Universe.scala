package scbaz;

import scala.xml._ ;

abstract class Universe(val name:String, val description:String) {
  def toXML : Node ;
  
  def retrieveAvailable() : PackageSet ;
}


object Universe {
  def fromXML(node:Node):Universe = {
    node match {
      case node:Elem => {
	val name = node.label;
	name match {
	  case "overrideuniverse" =>
	    OverrideUniverse.fromXML(node);

	  case "simpleuniverse" =>
	    SimpleUniverse.fromXML(node);

	  case "emptyuniverse" =>
	    new EmptyUniverse();

	  case _ =>
	    // XXX should be a ParseError
	    throw new Error("unknown universe type: " + name);
	}
      }

      // XXX should raise a ParseError of some kind
      case _ => throw new Error("not a valid Universe"); 
    }
  }
}
