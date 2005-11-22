package scbaz.messages;

import scala.xml._;

case class RemovePackage(spec:PackageSpec)
extends Message {
  override def toXML = 
    Elem(null, "removepackage", Null, TopScope,
	 spec.toXML)

}


object RemovePackageUtil {
  def fromXML(node:Node) = {
    val spec = PackageSpecUtil.fromXML((node \ "packagespec")(0));
    new RemovePackage(spec);
  }
}
