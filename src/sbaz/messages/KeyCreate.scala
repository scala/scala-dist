/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

package sbaz.messages

import sbaz._
import sbaz.keys._
import scala.xml._

/** Create a key for the specified description and message pattern */
case class KeyCreate(messages: MessagePattern, description: String) 
extends AbstractKeyMessage {
	def toXML = 
<keycreate>
  <messages>{messages.toXML}</messages>
  <description>{description}</description>
</keycreate>
}


object KeyCreateUtil {
  def fromXML(xml: Node): KeyCreate = {
    val messagesNode = (xml \\ "messages")(0)
    val messagesXML = messagesNode.child.find(_.isInstanceOf[Elem]) match {
      case None => throw new XMLFormatError(xml)
      case Some(m) => m
    }
    val messages = MessagePattern.fromXML(messagesXML)
    val description = (xml \\ "description").text
    new KeyCreate(messages, description)
  }
}
