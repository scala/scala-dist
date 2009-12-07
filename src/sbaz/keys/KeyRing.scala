/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$
package sbaz.keys

import scala.collection.mutable.HashSet
import scala.xml._

/** A mutable collection of Key's.  */
class KeyRing {
  def this(keys: Seq[Key]) = {
    this()
    addKeys(keys)
  }
  
  val keyHolder = new HashSet[Key]
                         
  def keys = keyHolder.toList
  
  def addKey(key: Key) = keyHolder += key
  def addKeys(keys: Seq[Key]) = keyHolder ++= keys
  def removeKey(key: Key) = keyHolder -= key
  
  def toXML: Node =
<keyring>{keys.map(_.toXML)}</keyring>
}
		

object KeyRing {
  def fromXML(node: Node): KeyRing = {
    val keysXML = node \\ "key"
    val keys = keysXML.toList.map(KeyUtil.fromXML)
    val ring = new KeyRing
    ring.addKeys(keys)
    ring
  }
}
