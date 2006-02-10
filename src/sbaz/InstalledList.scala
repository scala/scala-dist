package sbaz;

import scala.xml._ ;
import java.io.{StringReader} ;
import java.io.File ;

import ProposedChanges._
import scala.collection.immutable.Set

// A list of packages that are installed in a ManagedDirectory
//
// invariant: only one package with each name
//            may be present in the list at a time
class InstalledList {
  var installedEntries: List[InstalledEntry] = Nil  // XXX this should use a set of some kind for efficiency

  def packages = new PackageSet(installedEntries.map(.pack))
  
  // return a list of package specifications for everything installed
  def sortedPackageSpecs = {
    val specs = installedEntries.map(p => p.packageSpec);
    specs.sort((a,b) => a < b) ;
  }

  // find an entry with a specified name if there is one
  def entryNamed(name:String) : Option[InstalledEntry] = {
    installedEntries.find(p => p.name.equals(name))
  }


  def removeNamed(name: String) = {
    installedEntries = installedEntries.filter(p => !(p.name.equals(name)));
  }

  def remove(spec: PackageSpec) = {
    installedEntries = installedEntries.filter(p => !(p.packageSpec.equals(spec)));
  }

  def add(entry : InstalledEntry) = { 
    removeNamed(entry.name) ;
    installedEntries = entry :: installedEntries ;
  }

  def addAll(entries : List[InstalledEntry]):Unit = {
    for(val e <- entries) {
      add(e);
    }
  }

  
  // check whether a specified packages has been installed
  def includes(spec:PackageSpec):Boolean = {
    installedEntries.exists(p => p.packageSpec.equals(spec))
  }

  // check whether a package has all of its dependencies
  // already installed
  def includesDependenciesOf(pack:Package):Boolean = {
    ! pack.depends.exists(dep =>
      ! installedEntries.exists(p => p.name.equals(dep)))
  }


  // find all installed packages that depend on a specified package name
  def entriesDependingOn(packname:String):List[InstalledEntry] = {
    installedEntries.filter(p => p.depends.contains(packname));
  }

  // check whether any installed package depends on a
  // specified package name
  def anyDependOn(packname:String):Boolean = {
    !entriesDependingOn(packname).isEmpty
  }


  // find the entries that includes the specified filename, if any
  def entriesWithFile(file: Filename): List[InstalledEntry] = {
    // XXX this should use a hash table, not iterate over all files
    installedEntries.filter(p => p.files.contains(file));
  }
  
  // check whether a proposed sequence of changes is acceptible
  def changesAcceptible(changes: Seq[ProposedChange]): Boolean = {
    val newPackages = changes.elements.foldLeft[PackageSet](packages)((set, pc) => pc(set))
    true
  }

  def toXML : Node = {
    Elem(null, "installedlist", Null, TopScope,
	 (installedEntries.map(p => p.toXML)) : _* )
  }

  override def toString() = "InstalledList (" + installedEntries.toString() + ")";
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
      ("<installedlist>\n" +

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

       "</installedlist>") ;

    val node = XML.load(new StringReader(xml)) ;
    val list = InstalledList.fromXML(node) ;

    Console.println(list);
    Console.println(list.toXML);
  }
}
