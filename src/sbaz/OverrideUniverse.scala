package sbaz;

import java.net.URL ;
import scala.xml._ ;
import scala.collection.immutable._ ;

// An OverrideUniverse is used to combine multiple universes,
// where packages in later component universes are intended to override
// packages in earlier ones.  Packages in later universes cause all
// same-named packages in earlier universes to be invisible in the compound
// universe, even if they have different version numbers.
class OverrideUniverse(name0:String, description0:String,
		       val components:List[Universe])
extends Universe(name0, description0) {
  def retrieveAvailable() = { 
    val packages = components.foldLeft[List[AvailablePackage]](Nil)((packs,univ) => {
      val newPacks = univ.retrieveAvailable().packages;
      val newNames = ListSet.Empty[String].incl(newPacks.map(p => p.name));
      val oldMinus = packs.filter(p => ! newNames.contains(p.name));
      newPacks ::: oldMinus
    });
    new AvailableList(packages);
  }

  override def simpleUniverses =
    components.foldLeft(Nil:List[SimpleUniverse])((list,univ) =>
      list ::: univ.simpleUniverses);


  // XXX should remove List( from the printString...
  override def toString() =
    "OverrideUniverse(\"" + name + "\", " + components + ")";

  def toXML =
    Elem(null, "overrideuniverse", Null, TopScope,
	 Elem(null, "name", Null, TopScope,
	      Text(name)),
	 Elem(null, "description", Null, TopScope,
	      Text(description)),
	 Elem(null, "components", Null, TopScope,
	      (components.map(.toXML)) : _*))
}



object OverrideUniverse {
  def fromXML(node:Node) = {
    val name = (node \ "name")(0).child(0).toString(false);
    val description = (node \ "description")(0).child(0).toString(false);
    val componentNodes = (node \ "components")(0).child.toList ;
    val components = componentNodes.map(Universe.fromXML) ;

    new OverrideUniverse(name,description,components);
  }
}


object TestOverrideUniverse {
  def main(args:Array[String]):Unit = {
    val univ1 =
      new SimpleUniverse("scala-dev",
			 "development universe of Scala",
			 new URL("http://localhost/blah"));
    val univ2 = 
      new SimpleUniverse("local-hacks",
			 "some local hacks",
			 new URL("http://localhost/blah-local"));
    val univ =
      new OverrideUniverse("playground",
			   "a combination of public and private packages",
			   List(univ1, univ2));

    val xml = univ.toXML ;

    Console.println(univ);
    Console.println(xml);
    Console.println(Universe.fromXML(xml));
  }

}
