/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import scala.collection.Set
import scala.xml.{Elem, Node}

/** a list of AvailablePackage's */
class AvailableList(val available: List[AvailablePackage]) {
  def numPackages = available.length
  
 def packages = new PackageSet(available.map(_.pack))
 
  def sortedSpecs = {
    val specs = available.map(_.spec)
    specs.sortWith((a,b) => a < b)
  }


  def newestNamed(name : String) : Option[AvailablePackage] = {
    val matching = available.filter(_.name.equals(name))
    matching match {
      case Nil => None
      case _ => Some(matching.sortWith((p1,p2) => p1.version > p2.version).head)
    }
  }

  // Find a package with the specified spec.
  def packageWithSpec(spec: PackageSpec): Option[AvailablePackage] =
    available.find(p => p.spec.equals(spec))

  // Choose packages needed to install a given package specification,
  // including all dependencies, recursively.  The return value is
  // in reverse order of dependencies, so that installing the
  // packages in the sequence specified should not cause any
  // dependency errors.  If the package cannot be installed due
  // to dependency problems, the routine throws a DependencyError .
  def choosePackagesFor(spec: PackageSpec, alreadyHave: Set[String]): Seq[AvailablePackage] = {
    // XXX this should insist on returning packages in
    // reverse dependency order; I have not verified that
    // this algorithm does so

    val firstPack = packageWithSpec(spec) match {
      case Some(p) => p
      case None => throw new DependencyError()
    }

    chooseDependencyPackagesFor(firstPack.pack, alreadyHave) :+ firstPack
  }  
  
  def chooseDependencyPackagesFor(firstPack: Package, alreadyHave: Set[String]): Seq[AvailablePackage] = {
    var mightStillNeed: List[String] = firstPack.depends.toList
    var chosen: List[AvailablePackage] = Nil

    // mightStillNeed holds names of all packages that are depended on
    // by packages in chosen.  Some of the packages might already
    // be in chosen, however.  Care must be taken not to add
    // a package twice to chosen.

    while (mightStillNeed != Nil) {
      mightStillNeed match {
        case Nil => 
          ; //Dependency audits will throw errors later if unsatisfied
        case n :: r =>
          mightStillNeed = r

          if (!alreadyHave.contains(n) && !chosen.exists(p => p.name.equals(n))) {
            newestNamed(n) match {
              case None => 
                ; //throw new DependencyError()

              case Some(p) =>
                chosen = p :: chosen
                mightStillNeed = p.depends.toList ::: mightStillNeed
            }
          }
      }
    }
    return chosen
  }

  // Just like the other choosePackagesFor, but the newest package
  // of a given name is targeted, instead of a more specific package
  // with both the name and version specified.
  def choosePackagesFor(name: String, alreadyHave: Set[String]): Seq[AvailablePackage] =
    newestNamed(name) match {
      case Some(pack) => choosePackagesFor(pack.spec, alreadyHave)
      case None => throw new DependencyError()
    }

  override def toString() =
    "AvailableList (" + available.length + " packages)"

  def toXML =
<availableList>
  {available.map(_.toXML)}
</availableList>;

  def toOldXML = 
<packageset>
  {available.map(_.toOldXML)}
</packageset>;
}


object AvailableListUtil {
  def fromXML(node: Node) = node match {
    case node: Elem =>
      val packNodeName =
        node.label match {
          case "availableList" => "availablePackage"
          case "packageset" => "package"   // legacy format from version 1.0
          case _ => throw new FormatError()
        }
      val packs =
        (node \\ packNodeName).toList.map(
          AvailablePackageUtil.fromXML)

      new AvailableList(packs)

    case _ =>
      throw new FormatError()
  }
}
