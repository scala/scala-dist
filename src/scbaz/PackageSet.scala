package scbaz;

import scala.xml._ ;

class PackageSet(val packages: List[Package]) {
  override def toString() = {
    "PackageSet (" + packages + ")"
  }

  def newestNamed(name : String) : Option[Package] = {
    val matching = packages.filter(p => p.name.equals(name));
    matching match {
      case Nil => None ;
      case _ => Some(matching.sort ((p1,p2) => p1.version > p2.version) (0)) ;
    }
  }

  def choosePackagesFor(name:String) : Seq[Package] = {
    var chosen : List[Package] = Nil ;
    var mightStillNeed = name :: Nil ;

    while(true) {
      mightStillNeed match {
	case Nil => return chosen ;
	case n :: r => {
	  mightStillNeed = r;

	  if(! chosen.exists(p => p.name.equals(n))) {
	    newestNamed(n) match {
	      case None => ()  // XXX should throw an exception here
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
}



object PackageSet {
  def fromXML(xml:Elem) : PackageSet = {
    // XXX should use a DTD or schema or such
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
