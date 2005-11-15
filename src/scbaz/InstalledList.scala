package scbaz;

import scala.xml._ ;
import java.io.{StringReader} ;

// A list of packages that are installed in a ManagedDirectory
//
// invariant: only one package with each package spec (name+version)
//            may be present in the list at a time
class InstalledList {
  var packages:List[InstalledEntry] = Nil ;

  def remove(spec : PackageSpec) = {
    packages = packages.filter(p => p.packageSpec != spec)
  }

  def add(entry : InstalledEntry) = { 
    remove(entry.packageSpec) ;
    packages = entry :: packages ;
  }

  def addAll(entries : List[InstalledEntry]) = {
    entries.map(add) ;
  }


  def toXML : Node = {
    Elem(null, "installedlist", Null, TopScope,
	 (packages.map(p => p.toXML)) : _* )
  }

  override def toString() = packages.toString() ;
}


object InstalledList {
  def fromXML(xml:Node) : InstalledList = {
    val entryNodes = (xml \ "installedpackage").toList ;
    val entries = entryNodes.map(InstalledEntry.fromXML) ;

    val list = new InstalledList() ;
    list.addAll(entries) ;
    list
  }
}





object TestInstalledList {
  def main(args:Array[String]) = {
    val xml =
      "<installedlist>\n" +

      "<installedpackage>\n" +
      "<name>foo</name>\n" +
      "<version>1.5</version>\n" +
      "<files>\n" +
      "  <filename>lib/foo.jar</filename>\n" +
      "  <filename>doc/foo/foo.html</filename>\n" +
      "</files>\n" +
      "<complete/>\n" +
      "</installedpackage>\n" +

      "<installedpackage>\n" +
      "<name>bar</name>\n" +
      "<version>1.1</version>\n" +
      "<files>\n" +
      "  <filename>lib/bar.jar</filename>\n" +
      "  <filename>doc/bar/index.html</filename>\n" +
      "</files>\n" +
      "</installedpackage>\n" +

      "</installedlist>" ;

    val node = XML.load(new StringReader(xml)) ;
    val list = InstalledList.fromXML(node) ;

    Console.println(list);
    Console.println(list.toXML);
  }
}
