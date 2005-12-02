package sbaz.messages;

import scala.xml._;

// A message saying that something has gone wrong with
// the previous request.
case class NotOK(explanation: String)
extends Message {
  override def toXML =  <notok>{explanation}</notok> ;
}


object NotOKUtil {
  def fromXML(node:Node) = {
    val explanation = node.text ;
    NotOK(explanation)
  }
}
