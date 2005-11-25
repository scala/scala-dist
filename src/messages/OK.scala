package scbaz.messages;

import scala.xml._;

case class OK()
extends Message {
  override def toXML =  <ok/> ;
}


object OKUtil {
  def fromXML(node:Node) = OK();
}
