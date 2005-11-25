package scbaz;

import scala.xml._ ;
import java.io.{StringReader} ;

// A list of packages that are installed in a ManagedDirectory
//
// invariant: only one package with each package spec (name+version)
//            may be present in the list at a time
class InstalledList {
  var packages:List[InstalledEntry] = Nil ;

  // return a list of package specifications for everything installed
  def sortedPackageSpecs = {
    val specs = packages.map(p => p.packageSpec);
    specs.sort((a,b) => a < b) ;
  }

  // find an entry with a specified name if there is one
  def entryNamed(name:String) : Option[InstalledEntry] = {
    packages.find(p => p.name.equals(name))
  }


  def remove(spec : PackageSpec) = {
    packages = packages.filter(p => !(p.packageSpec.equals(spec)))
  }

  def add(entry : InstalledEntry) = { 
    remove(entry.packageSpec) ;
    packages = entry :: packages ;
  }

  def addAll(entries : List[InstalledEntry]):Unit = {
    for(val e <- entries) {
      add(e);
    }
  }

  
  // check whether a specified packages has been installed
  def includes(spec:PackageSpec):Boolean = {
    packages.exists(p => p.packageSpec.equals(spec))
  }

  // check whether a package has all of its dependencies
  // already installed
  def includesDependenciesOf(pack:Package):Boolean = {
    ! pack.depends.exists(dep =>
      ! packages.exists(p => p.name.equals(dep)))
  }


  // find all installed packages that depend on a specified package name
  def entriesDependingOn(packname:String):List[InstalledEntry] = {
    packages.filter(p => p.depends.contains(packname));
  }

  // check whether any installed package depends on a
  // specified package name
  def anyDependOn(packname:String):Boolean = {
    !entriesDependingOn(packname).isEmpty
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
    val entries = entryNodes.map(InstalledEntryUtil.fromXML) ;

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
