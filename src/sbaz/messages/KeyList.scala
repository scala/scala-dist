package sbaz.messages
import sbaz.keys._
import scala.xml._

/** Return a list of available keys */
case class KeyList(keyList: List[Key]) extends AbstractKeyMessage {
	def toXML = <keylist>{keyList.map(.toXML)}</keylist>
}

object KeyListUtil {
  def fromXML(node: Node) = {
    val keysXML = node \\ "key"
    val keys = keysXML.toList.map(KeyUtil.fromXML)
    new KeyList(keys)
  }
}
