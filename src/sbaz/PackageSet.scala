/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import scala.collection.immutable.{Set, Map, TreeMap, ListSet}
import scala.xml._ 

// A PackageSet includes a set of packages.  It does not allow including
// more than one Package with the spam spec, in order to simplify
// implementation.
class PackageSet(specToPack: Map[PackageSpec, Package]) extends Set[Package] {
  // XXX leaving off the [PackageSpec, Package] causes a compiler crash
  def this() = this(TreeMap.empty[PackageSpec, Package](Ordering.ordered[PackageSpec]))
      
  def this(packages: Seq[Package]) =
    this(TreeMap.empty[PackageSpec, Package](Ordering.ordered[PackageSpec]) ++
         packages.iterator.map(p => (p.spec, p)).toList)

  /*** methods for implementing Set ***/
  def +(pack: Package) = new PackageSet(specToPack + (pack.spec -> pack))
  def -(pack: Package) = new PackageSet(specToPack - pack.spec)
  def contains(pack: Package) = specToPack.contains(pack.spec)
  def iterator = specToPack.valuesIterator
  override def size = specToPack.size
  def empty[B] = new ListSet[B]
  
  /* Return the packages as a list, for compatibility.  New code should use PackageSet's
     directly */      
  def packages: List[Package] = toList
  
  def withoutSpec(spec: PackageSpec) =
    new PackageSet(specToPack - spec)
  
  override def toString() =
    "PackageSet (" + packages + ")"

  def sortedSpecs = {
    val specs = packages.map(_.spec);
    specs.sortWith((a,b) => a < b) ;
  }

  // find the newest package with the specified name
  def newestNamed(name : String) : Option[Package] = {
    val matching = packages.filter(p => p.name.equals(name));
    matching match {
      case Nil => None ;
      case _ => Some(matching.sortWith((p1,p2) => p1.version > p2.version).head) ;
    }
  }
  
  // check whether a package of a given name is present
  def includesPackageNamed(name: String) = !newestNamed(name).isEmpty

  // Find a package with the specified spec.  Throws an
  // Exception if none is present.
  // XXX which exception?  it is whatever collections throw
  // when the requested element isn't there...
  def packageWithSpec(spec: PackageSpec): Option[Package] =
    packages.find(_.spec.equals(spec))

  // Choose packages needed to install a given package specification,
  // including all dependencies, recursively.  The return value is
  // in reverse order of dependencies, so that installing the
  // packages in the sequence specified should not cause any
  // dependency errors.  If the package cannot be installed due
  // to dependency problems, the routine throws a DependencyError .
  def choosePackagesFor(spec: PackageSpec): Seq[Package] = {
    // XXX this should insist on returning packages in
    // reverse dependency order; I have not verified that
    // this algorithm does so

    val firstPack = packageWithSpec(spec) match {
      case Some(p) => p
      case None => throw new DependencyError()
    }

    var chosen: List[Package] = firstPack :: Nil
    var mightStillNeed: List[String] = firstPack.depends.toList

    // mightStillNeed holds names of all packages that are depended on
    // by packages in chosen.  Some of the packages might already
    // be in chosen, however.  Care must be taken not to add
    // a package twice to chosen.

    while (true) {
      mightStillNeed match {
        case Nil =>
          return chosen
        case n :: r =>
          mightStillNeed = r

          if (! chosen.exists(p => p.name.equals(n))) {
            newestNamed(n) match {
              case None =>
                throw new DependencyError()
              case Some(p) =>
                chosen = p :: chosen
                mightStillNeed = p.depends.toList ::: mightStillNeed
            }
          }
      }
    }

    Nil  // never reached; just making the compiler happy
  }

  // Just like the other choosePackagesFor, but the newest package
  // of a given name is targeted, instead of a more specific package
  // with both the name and version specified.
  def choosePackagesFor(name: String): Seq[Package] =
    newestNamed(name) match {
      case Some(pack) => choosePackagesFor(pack.spec)
      case None => throw new DependencyError()
    }

  def toXML = {
    Elem(null, "packageset", Null, TopScope,
	 (packages.map(_.toXML)) : _* )
  }
}

object PackageSet {
  def fromXML(xml: Node): PackageSet = {
    val packXMLs = xml \ "package"
    val packs = packXMLs.toList.map(PackageUtil.fromXML)
    return new PackageSet(packs)
  }
  val Empty = new PackageSet(Nil)
}

object TestPackageSet {
  def main(args:Array[String]) = {
    val reader = new java.io.FileReader("/home/lex/public_html/expacks/packages")
    val node = XML.load(reader)
    val packageSet = PackageSet.fromXML(node)
    Console.println(packageSet)
    Console.println(packageSet.newestNamed("sbaz"))
    Console.println(packageSet.choosePackagesFor("sbaz"))
  }
}
