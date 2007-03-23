/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.messages

import scala.xml._

case class SendPackageList()
extends Message {
  override def toXML =  <sendpackagelist/> ;
}


object SendPackageListUtil {
  def fromXML(node: Node) = SendPackageList()
}
