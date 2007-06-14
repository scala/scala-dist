/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$
package sbaz.messages

import sbaz.keys._
import scala.xml._

/** Return a list of available keys */
case class KeyList(keyList: List[Key]) extends AbstractKeyMessage {
  def toXML = <keylist>{keyList.map(_.toXML)}</keylist>
}

object KeyListUtil {
  def fromXML(node: Node) = {
    val keysXML = node \\ "key"
    val keys = keysXML.toList.map(KeyUtil.fromXML)
    new KeyList(keys)
  }
}
