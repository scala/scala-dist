/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import scala.xml._
import java.net.URL


// a package that is available for download.
class AvailablePackage(val pack: Package, val link: URL) {
  override def toString(): String = pack.toString()

  def toXML: Node =
<availablePackage>
  {pack.toXML}
  <link>{link.toString()}</link>
</availablePackage>;

  def toOldXML: Node =
    Elem(null, "package", Null, TopScope,
	 Elem(null, "name", Null, TopScope,
 	      Text(name)),
	 Elem(null, "version", Null, TopScope,
 	      Text(version.toString())),
 	 Elem(null, "link", Null, TopScope,
 	      Text(link.toString())),
 	 Elem(null, "depends", Null, TopScope,
 	      (depends.toList.map
	       (x => Elem(null, "name", Null, TopScope, Text(x)))):_*),
 	 Elem(null, "description", Null, TopScope,
 	      Text(description)));



  def name = pack.name
  def version = pack.version
  def depends = pack.depends
  def description = pack.description
  def filename = pack.filename

  def spec = pack.spec
}


object AvailablePackageUtil {
  def fromXML(node: Node) =
    node match {
      case node:Elem =>
	node.label match {
	  case "availablePackage" =>
	    val packNode = (node \\ "package")(0)
	    val linkNode = (node \\ "link")(0)
	    val pack = PackageUtil.fromXML(packNode)
	    val link = new URL(linkNode.text)

	    new AvailablePackage(pack, link)

	  case "package" =>
	    // legacy format from version 1.0
	    val pack = PackageUtil.fromXML(node)
	    val linkNode = (node \\ "link")(0)
	    val link = new URL(linkNode.text)

	    new AvailablePackage(pack, link)

	  case _ => throw new FormatError()
	}

        case _ => throw new FormatError()
    }

}

