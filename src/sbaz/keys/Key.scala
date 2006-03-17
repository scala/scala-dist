package sbaz.keys
import scala.xml._



/** A key used to access resources on a bazaar server */
case class Key(messages: MessagePattern, description: String,  data: String) {
  def toXML =
<key>
  <messages>
    {messages.toXML}
  </messages>
  <description>{description}</description>
  <data>{data}</data>
</key>
}


object KeyUtil {  // XXX bah, still having problems with case classes and same-named objects
  def fromXML(xml: Node): Key = {
    // XXX this should be more careful about throwing FormatError's
    val messagesNode = (xml \\ "messages")(0)
    val messagesXML = messagesNode.child.find(.isInstanceOf[Elem]) match {
      case None => throw new XMLFormatError(xml)
      case Some(m) => m
    }
    val messages = MessagePattern.fromXML(messagesXML)
    val description = (xml \\ "description").text
    val data = (xml \\ "data").text
    new Key(messages, description, data)
  }

  val random = new java.security.SecureRandom
  
  /** Generate random data suitable for a key.  All characters
    * in the returned string will be integers
    */
  def genKeyData: String = {
    import Math.abs
    "" + abs(random.nextLong) + abs(random.nextLong)
  }
}

