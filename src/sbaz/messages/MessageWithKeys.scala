/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.messages

import sbaz.keys._
import scala.xml._

case class MessageWithKeys(override val authKeys: List[Key],
                           override val sansKeys: Message)
extends Message
{
  def toXML =
<messagewithkeys>
  <keys>
    {authKeys.map(_.toXML)}
  </keys>
  <message>
    {sansKeys.toXML}
  </message>
</messagewithkeys>
}


object MessageWithKeysUtil {
  def fromXML(xml: Node) = {
    val keysXML = xml \ "keys" \ "key"
    val keys = keysXML.toList.map(KeyUtil.fromXML)
    val messageXML = (xml \ "message")(0).child.find(_.isInstanceOf[Elem]).get
    val message = MessageUtil.fromXML(messageXML)
    MessageWithKeys(keys, message)
  }
}
