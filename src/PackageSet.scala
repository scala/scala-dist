package scbaz;

import scala.xml._ ;


// XXX either this needs to be mutable, or it needs
//     to support efficient updates by using some non-list
//     structure underneath....
class PackageSet(val packages: List[Package]) {
  override def toString() = {
    "PackageSet (" + packages + ")"
  }

  def sortedSpecs = {
    val specs = packages.map(p => p.spec);
    specs.sort((a,b) => a < b) ;
  }


  def newestNamed(name : String) : Option[Package] = {
    val matching = packages.filter(p => p.name.equals(name));
    matching match {
      case Nil => None ;
      case _ => Some(matching.sort ((p1,p2) => p1.version > p2.version) (0)) ;
    }
  }

  // Find a package with the specified spec.  Throws an
  // Exception if none is present.
  // XXX which exception?  it is whatever collections throw
  // when the requested element isn't there...
  def packageWithSpec(spec: PackageSpec): Option[Package] = {
    packages.find(p => p.spec.equals(spec))
  }

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
      case Some(p) => p;
      case None => throw new DependencyError();
    };

    var chosen : List[Package] = firstPack :: Nil ;
    var mightStillNeed: List[String] = Nil ;

    while(true) {
      mightStillNeed match {
	case Nil => return chosen ;
	case n :: r => {
	  mightStillNeed = r;

	  if(! chosen.exists(p => p.name.equals(n))) {
	    newestNamed(n) match {
	      case None => throw new Error("no available package named " + n);
		                  // XXX this should be a DependencyError
	      case Some(p) => {
		chosen = p :: chosen;
		mightStillNeed = p.depends.toList ::: mightStillNeed;
	      }
	    }
	  }
	} 
      }
    }

    return Nil ;  // never reached; just making the compiler happy
  }

  // Just like the other choosePackagesFor, but the newest package
  // of a given name is targeted, instead of a more specific package
  // with both the name and version specified.
  def choosePackagesFor(name:String) : Seq[Package] = {
    newestNamed(name) match {
      case Some(pack) => choosePackagesFor(pack.spec);
      case None => throw new DependencyError();
    }
  }

  def toXML = {
    Elem(null, "packageset", Null, TopScope,
	 (packages.map(.toXML)) : _* )
  }
}



object PackageSet {
  def fromXML(xml: Node) : PackageSet = {
    val packXMLs = xml \ "package" ;
    val packs = packXMLs.toList.map(Package.fromXML) ;
    return new PackageSet(packs) ;
  }

  val Empty = new PackageSet(Nil);
}


object TestPackageSet {
  def main(args:Array[String]) = {
    val reader = new java.io.FileReader("/home/lex/public_html/expacks/packages");
    val node = XML.load(reader) ;
    val packageSet = PackageSet.fromXML(node) ;

    Console.println(packageSet) ;


    Console.println(packageSet.newestNamed("scbaz"));
    Console.println(packageSet.choosePackagesFor("scbaz"));
  }
}
