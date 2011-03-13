package sbaz.keys.msgpatt
import scala.xml._
import sbaz._
import sbaz.keys._
import sbaz.messages._

/** Permission to edit advertised packages whose name
  * matches the specified regex
  */
case class Edit(nameRegex: String) extends MessagePattern {
  def matches(msg: Message) =
    msg match {
      case AddPackage(pack) => pack.name.matches(nameRegex)
      case RemovePackage(pack) => pack.name.matches(nameRegex)
      case _ => false
    }
  
	def toXML = <edit nameregex={nameRegex}/>
}

object EditUtil {
  def fromXML(xml: Node) = {
    xml match {
      case xml: Elem =>
        xml.attribute("nameregex") match {
          case None => throw new XMLFormatError(xml)
          case Some(reg) => Edit(reg.text)
        }
      case _ => throw new XMLFormatError(xml)
    }
  }
}