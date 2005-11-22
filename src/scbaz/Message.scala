package scbaz;

import scbaz.messages._ ;
import scala.xml._ ;

abstract class Message {
  def toXML : Node ;

  // key
  // applyToClient
  // applyToServer
}

object Message {
  def fromXML(node:Node):Message = {
    node match {
      case node:Elem =>
	node.label match {
	  case "addpackage" => AddPackageUtil.fromXML(node);
	  case "removepackage" => RemovePackageUtil.fromXML(node);

	  case _ => throw new Error("not a valid Message");
	}

      // XXX ParseError
      case _ => throw new Error("not a valid Message");
    }
  }
}
