package sbaz;

import scala.xml._;
import java.net.URL;


// a package that is available for download.
class AvailablePackage(val pack: Package, val link: URL) {
  override def toString(): String = pack.toString();

  def toXML: Node =
<availablePackage>
  <package>{pack.toXML}</package>
  <link>{link}</link>
</availablePackage>;


  def name = pack.name;
  def version = pack.version;
  def depends = pack.depends;
  def description = pack.description;
  def filename = pack.filename;

  def spec = pack.spec;
}


object AvailablePackageUtil {
  def fromXML(node: Node) = {
    node match {
      case node:Elem =>
	node.label match {
	  case "availablepackage" => {
	    // XXX this should be more careful about throwing FormatError's
	    // that is best handled using XML patterns, though, which aren't
	    // supported very well at the moment....
	    val packNode = (node \\ "package")(0);
	    val linkNode = (node \\ "link")(0);

	    val pack = PackageUtil.fromXML(packNode);
	    val link = new URL(linkNode.text);

	    new AvailablePackage(pack, link);
	  };

	  case "package" => {
	    // legacy format from version 1.0
	    val pack = PackageUtil.fromXML(node);
	    val linkNode = (node \\ "link")(0);
	    val link = new URL(linkNode.text);

	    new AvailablePackage(pack, link);
	  };

	  case _ => throw new FormatError();
	};

        case _ => throw new FormatError();
    }
  };
}

