package scbaz.messages;

import scala.xml._;


// A message from the server to the client listing all packages
// currently listed on the server.
case class LatestPackages(packages:PackageSet)
extends Message {
  override def toXML: Node = 
<latestpackages>
  { packages.toXML }
</latestpackages> ;
}


object LatestPackageSetUtil {
  def fromXML(node: Node) = {
    val packsXML = (node \ "packageset")(0) ;
    val packs = PackageSet.fromXML(packsXML);
    new LatestPackages(packs)
  }
}
