package sbaz

import java.io.{File, StringReader} 
import scala.xml._ 
import scala.collection.immutable._ 

// Information about one package that is currently installed.
class InstalledEntry(val pack: Package, val files: List[File])
{
  def name = pack.name
  def version = pack.version
  def description = pack.description
  def depends = pack.depends

  val packageSpec = PackageSpec(name, version) 
  

  def toXML:Node = {
<installedpackage>
  {pack}
  <files>{files.map(f => <filename>{f.getPath()}</filename>)}</files>
</installedpackage>
	  }

  override def toString() =
    packageSpec.toString +
     " (" + files.length + " files)"
}


object InstalledEntryUtil {
   def fromOldXML(xml:Node) = {
     // XXX need to throw a reasonable error for malformed input
     val parts = xml 
     val name = (parts \ "name").text
     val version = new Version((parts \ "version").text)
     val dependsList =
       (parts \ "depends" \ "name").toList
       .map(nod => nod.text)
     val depends = ListSet.Empty[String].incl(dependsList) 
     val files =
       for{val node <- (xml \ "files" \ "filename").elements}
         yield new File(node.text)

     new InstalledEntry(
         new Package(name, version, depends, "(description not available)"),
         files.toList)
   }
   
   def fromXML(xml:Node): InstalledEntry = {
     if((xml \ "package").length == 0)
       return fromOldXML(xml)
       
     val pack = PackageUtil.fromXML((xml \ "package")(0))
     val files =
       for{val node <- (xml \ "files" \ "filename").elements}
     		yield new File(node.text)
         
     new InstalledEntry(pack, files.toList)
   }
}
