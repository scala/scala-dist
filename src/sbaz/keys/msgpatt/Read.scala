package sbaz.keys.msgpatt
import sbaz.messages._

/** Matches all read-only messages, namely, SendPackageList */
case object Read extends MessagePattern {
  def matches(msg: Message) =
    msg match {
      case SendPackageList() => true
      case _ => false
    }

  def toXML = <read/>
}
