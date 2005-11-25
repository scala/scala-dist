package scbaz.messages;

import scala.xml._;

case class SendPackageList()
extends Message {
  override def toXML =  <sendpackagelist/> ;
}


object SendPackageListUtil {
  def fromXML(node:Node) = SendPackageList();
}
