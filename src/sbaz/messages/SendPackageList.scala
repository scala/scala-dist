/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.messages

import sbaz._
import scala.xml._

case class SendPackageList()
extends Message {
  override def toXML =  <sendpackagelist/> ;
}


object SendPackageListUtil {
  def fromXML(node: Node) = SendPackageList()
}
