/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import scala.collection.immutable._
import scala.xml._

// XXX compiler error if I make this a case class
class Package(val name: String,
	      val version: Version,
	      val depends: Set[String],
	      val description: String)
{
  def spec = new PackageSpec(name, version)

  /** Return a canonical filename for this package */
  def filename: String =
    name.replace(' ', '_') + "-" + version + ".zip"

  def longDescription: String = 
    ("Name: " + name + "\n" +
     "Version: " + version + "\n" +
     "Depends: " + depends.mkString(", ") + "\n" +
     "Description:\n" + description + "\n");
  
  override def toString() = spec.toString()

  def toXML : Node = {
    Elem(null, "package", Null, TopScope,
	 Elem(null, "name", Null, TopScope,
 	      Text(name)),
	 Elem(null, "version", Null, TopScope,
 	      Text(version.toString())),
 	 Elem(null, "depends", Null, TopScope,
 	      (depends.toList.map
	       (x => Elem(null, "name", Null, TopScope, Text(x)))):_*),
 	 Elem(null, "description", Null, TopScope,
 	      Text(description)))
  }
}


object PackageUtil {
  def fromXML (node: Node): Package = {
    val name =  (node \ "name")(0).child(0).text
    val version = new Version((node \ "version")(0).child(0).text)
    val description = (node \ "description")(0).child(0).text

    val dependsList = ((node \ "depends") \ "name")
                      .toList.map(n => n(0).child(0).text)

    val depends = dependsList.foldLeft(ListSet.empty[String])((x,y) => x+y)

    return new Package(name,
		       version,
		       depends,
		       description)
  }
  
  /** Check a package name string.  If there is a problem, returns Some(why)
   * where why is an explanation of the problem.  If the string is
   * fine, it returns None.
   */
 def checkName(str: String): Option[String] = {
   def ok(c: Char): Boolean = {
     (c >= 'a' && c <= 'z') ||
     (c >= 'A' && c <= 'Z') ||
     (c >= '0' && c <= '9') ||
     (c == '-')
   }
     
   for (val i <- Iterator.range(0, str.length); val c=str.charAt(i); !ok(c))
     return Some("Invalid character for a package name (" + c + ")")
     
   None
 }

}


// XXX this object should be in ../tests/ or ../examples/ 
object TestPackage {
  def main(args: Array[String]) = {
    val xml = 
      ("<package>\n" +
       "<name>DDP docs</name>\n" +
       "<version>2005-11-09</version>\n" +
       "<link>http://www.lexspoon.org/ti/index.html</link>\n" +
       "<depends>\n" +
       "<name>DDP tech report</name>\n" +
       "<name>DDP dissertation</name>\n" +
       "</depends>\n" +
       "<description>(meta-package) A variety of docs about the DDP type\n" +
       "inference framework.</description>\n" +
       "</package>\n") ;
    val reader = new java.io.StringReader(xml)
    val node = XML.load(reader)

    val pack = PackageUtil.fromXML(node)

    Console.println(pack)
    Console.println(pack.name)
    Console.println(pack.version)
    Console.println(pack.filename)
    Console.println(pack.depends)
    Console.println(pack.description)

    Console.println(pack.toXML)
  }
}

