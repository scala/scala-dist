package scbaz.messages;

import scala.xml._;

// something has gone wrong
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
