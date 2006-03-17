package sbaz.keys.msgpatt
import sbaz.messages._

/** Matches all key operations */
case object EditKeys extends MessagePattern {
  def matches(msg: Message) = msg.isInstanceOf[AbstractKeyMessage]

	def toXML = <editkeys/>
}
