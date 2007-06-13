/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.net.URL
import scala.xml._
import scala.collection.immutable._

// An OverrideUniverse is used to combine multiple universes,
// where packages in later component universes are intended to override
// packages in earlier ones.  Packages in later universes cause all
// same-named packages in earlier universes to be invisible in the compound
// universe, even if they have different version numbers.
class OverrideUniverse(name0: String, description0: String,
		       val components: List[Universe])
extends Universe(name0, description0) {
  def this(components: List[Universe]) =
    this("noname", "(no description)", components)

  def retrieveAvailable() = {
    val packages = components.foldLeft[List[AvailablePackage]](Nil)((packs, univ) => {
      val newPacks = univ.retrieveAvailable().available;
      val newNames = ListSet.empty[String].incl(newPacks.map(p => p.name));
      val oldMinus = packs.filter(p => ! newNames.contains(p.name));
      newPacks ::: oldMinus
    })
    new AvailableList(packages)
  }

  override def simpleUniverses =
    components.foldLeft(Nil:List[SimpleUniverse])((list,univ) =>
      list ::: univ.simpleUniverses);


  override def toString() =
    "OverrideUniverse(\"" + name + "\", " + components.mkString(", ") + ")"

  def toXML =
    Elem(null, "overrideuniverse", Null, TopScope,
	 Elem(null, "name", Null, TopScope,
	      Text(name)),
	 Elem(null, "description", Null, TopScope,
	      Text(description)),
	 Elem(null, "components", Null, TopScope,
	      (components.map(_.toXML)) : _*))
}



object OverrideUniverse {
  def fromXML(node: Node) = {
    val componentNodes = (node \ "components")(0).child.toList.filter(
      n => n.isInstanceOf[Elem])

    val components = componentNodes.map(Universe.fromXML)

    new OverrideUniverse(components)
  }
}
