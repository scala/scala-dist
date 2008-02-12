/* SBaz -- Scala Bazaar
 * Copyright 2005-2008 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import scala.collection.immutable._
import scala.xml.{Elem, Node}

// An OverrideUniverse is used to combine multiple universes,
// where packages in later component universes are intended to override
// packages in earlier ones.  Packages in later universes cause all
// same-named packages in earlier universes to be invisible in the compound
// universe, even if they have different version numbers.
class OverrideUniverse(val components: List[Universe]) extends Universe {
  def retrieveAvailable() = {
    val packages = components.foldLeft[List[AvailablePackage]](Nil)((packs, univ) => {
      val newPacks = univ.retrieveAvailable().available;
      val newNames = ListSet.empty[String] ++ (newPacks map (_.name))
      val oldMinus = packs.filter(p => ! newNames.contains(p.name))
      newPacks ::: oldMinus
    })
    new AvailableList(packages)
  }

  override def simpleUniverses =
    components.foldLeft(Nil: List[SimpleUniverse])((list,univ) =>
      list ::: univ.simpleUniverses);

  override def toString() =
    "OverrideUniverse(" + components.mkString(", ") + ")"

  def toXML =
<overrideuniverse>
  <components>
    {components.map(_.toXML)}
  </components>
</overrideuniverse>
}



object OverrideUniverse {
  def fromXML(node: Node) = {
    val componentNodes = (node \ "components")(0).child.toList.filter(
      n => n.isInstanceOf[Elem])

    val components = componentNodes.map(Universe.fromXML)

    new OverrideUniverse(components)
  }
}
