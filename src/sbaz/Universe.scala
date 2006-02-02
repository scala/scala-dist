package sbaz;

import scala.xml._ ;


// XXX remove the name and description from here.  The description
// should die and the name should only be in simple universe.
abstract class Universe(val name:String, val description:String) {
  def toXML : Node ;
  
  def retrieveAvailable() : AvailableList ;

  def simpleUniverses : List[SimpleUniverse] ;
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
	    SimpleUniverseUtil.fromXML(node);

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
