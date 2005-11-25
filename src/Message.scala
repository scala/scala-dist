package scbaz;

import scbaz.messages._ ;
import scala.xml._ ;


// A message that can be sent across a MessageStream.
abstract class Message {
  def toXML : Node ;

  // key
  // applyToClient
  // applyToServer
}


// XXX naming it Message crashes the compiler
object MessageUtil {
  def fromXML(node:Node):Message = {
    node match {
      case node:Elem =>
	node.label match {
	  case "addpackage" => AddPackageUtil.fromXML(node);
	  case "removepackage" => RemovePackageUtil.fromXML(node);
	  case "sendpackagelist" => SendPackageListUtil.fromXML(node);

	  case "latestpackages" => LatestPackagesUtil.fromXML(node);
	  case "ok" => OKUtil.fromXML(node);

	  case _ => throw new Error("not a valid Message");
	}

      // XXX ParseError
      case _ => throw new Error("not a valid Message");
    }
  }
}
