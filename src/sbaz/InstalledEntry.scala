/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.io.{File, StringReader} 
import scala.xml._ 
import scala.collection.immutable._ 

// Information about one package that is currently installed.
//
// Compatibility note: entries loaded from legacy installations
// can have installed Filename's that say they are files but really
// are directories.  Calling code should be tolerant of this.
class InstalledEntry(val pack: Package, val files: List[Filename])
{
  def name = pack.name
  def version = pack.version
  def description = pack.description
  def depends = pack.depends

  val packageSpec = PackageSpec(name, version) 
  

  def toXML:Node = {
<installedpackage>
  {pack.toXML}
  <files>{files.map(.toXML)}</files>
</installedpackage>
	  }

  override def toString() =
    packageSpec.toString +
     " (" + files.length + " files)"
}


object InstalledEntryUtil {
   def fromOldXML(xml: Node) = {
     // XXX need to throw a reasonable error for malformed input
     val parts = xml 
     val name = (parts \ "name").text
     val version = new Version((parts \ "version").text)
     val dependsList =
       (parts \ "depends" \ "name").toList
       .map(nod => nod.text)
     val depends = ListSet.empty[String].incl(dependsList) 
     val files =
       for (val node <- (xml \ "files" \ "filename").elements)
         yield Filename.fromXML(node)

     new InstalledEntry(
         new Package(name, version, depends, "(description not available)"),
         files.toList)
   }
   
   def fromXML(xml: Node): InstalledEntry = {
     if((xml \ "package").length == 0)
       return fromOldXML(xml)
       
     val pack = PackageUtil.fromXML((xml \ "package")(0))
     val files =
       for{val node <- (xml \ "files" \ "filename").elements}
     		yield Filename.fromXML(node)
         
     new InstalledEntry(pack, files.toList)
   }
}
