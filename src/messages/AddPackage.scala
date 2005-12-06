package sbaz.messages;

import scala.xml._;

case class AddPackage(pack:AvailablePackage)
extends Message {
  override def toXML = 
    Elem(null, "addpackage", Null, TopScope,
	 pack.toXML)

}


object AddPackageUtil {
  def fromXML(node:Node) = {
    val pack = AvailablePackageUtil.fromXML((node \ "package")(0));
    new AddPackage(pack);
  }
}
