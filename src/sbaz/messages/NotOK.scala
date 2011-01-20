/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.messages

import sbaz._
import scala.xml._

// A message saying that something has gone wrong with
// the previous request.
case class NotOK(explanation: String) extends Message {
  override def toXML =  <notok>{explanation}</notok> ;
}

object NotOKUtil {
  def fromXML(node:Node) = {
    val explanation = node.text
    NotOK(explanation)
  }
}
