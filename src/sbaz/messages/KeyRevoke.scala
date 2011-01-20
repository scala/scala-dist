/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.messages

import sbaz.keys._
import scala.xml._

/** A message requesting that a key be revoked */
case class KeyRevoke(key: Key) extends AbstractKeyMessage {
  def toXML = <keyrevoke>{key.toXML}</keyrevoke>
}


object KeyRevokeUtil {
  def fromXML(xml: Node) = {
    val key = KeyUtil.fromXML((xml \\ "key")(0))
    new KeyRevoke(key)
  }
}
