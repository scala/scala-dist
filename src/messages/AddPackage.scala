package scbaz.messages;

import scala.xml._;

case class AddPackage(pack:Package)
extends Message {
  override def toXML = 
    Elem(null, "addpackage", Null, TopScope,
	 pack.toXML)

}


object AddPackageUtil {
  def fromXML(node:Node) = {
    val pack = PackageUtil.fromXML((node \ "package")(0));
    new AddPackage(pack);
  }
}
