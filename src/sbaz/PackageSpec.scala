/* SBAZ -- the Scala Bazaar
 * Copyright 2005-2006 LAMP/EPFL
 * @author  Lex Spoon
 */
// $Id$

package sbaz

import scala.xml._

// A specification of a package.  It includes sufficient
// information to designate a package from a universe, but
// it does not include all of the information in a Package
case class PackageSpec(name: String, version: Version) 
extends Ordered[PackageSpec]
{
  override def toString() = toSlashNotation ;
  
  def compare(that: PackageSpec): int =
    that match {
    case PackageSpec(name2, version2) =>
      if(name < name2)
        -1
      else if(name > name2)
        1
      else { // name == name2
       if(version < version2)
         -1
       else if(version == version2)
          0
       else  // version > version2
         1
      }

    case _ => -that.compareTo(this)
  }


  def toSlashNotation = name + "/" + version ;

  def toXML:Node = {
    Elem(null, "packagespec", Null, TopScope,
      Elem(null, "name", Null, TopScope,
           Text(name)),
      Elem(null, "version", Null, TopScope,
           Text(version.toString())));
  }
}

object PackageSpecUtil {
  // parse a PackageSpec from the notation name/version .  If the
  // string is not in this format, then a FormatError is raised.
  def fromSlashNotation(str: String): PackageSpec = {
    val parts = str.split("/")
    if(parts.length == 2) {
      val name = parts(0)
      val rawVersion = parts(1)
      PackageSpec(name, new Version(rawVersion))
    } else {
      throw new FormatError()
    }
  }


  def fromXML(node:Node) = {
    val name = (node \ "name")(0).child(0).text
    val versionString = (node \ "version")(0).child(0)text

    val version = new Version(versionString);

    PackageSpec(name,version)
  }
}


object TestPackageSpec {
  def main(args:Array[String]):Unit = {
    val version1 = new Version("1.4") ;
    val version2 = new Version("1" + ".4") ;
    val name1 = "hello" ;
    val name2 = "he" + "llo";

    val spec1 = PackageSpec(name1, version1);
    val spec2 = PackageSpec(name2, version2);

    Console.print(spec1.toString() + " equals " + spec2 + "? ");
    Console.println(spec1.equals(spec2));
  }
}
