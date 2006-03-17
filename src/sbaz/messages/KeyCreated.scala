package sbaz.messages
import sbaz.keys._
import scala.xml._

case class KeyCreated(key: Key) extends AbstractKeyMessage {
	def toXML = <keycreated>{key.toXML}</keycreated>
}


object KeyCreatedUtil {
  def fromXML(xml: Node) = {
    val key = KeyUtil.fromXML((xml \\ "key")(0))
    new KeyCreated(key)
  }
}