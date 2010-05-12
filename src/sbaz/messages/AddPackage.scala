/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.messages

import sbaz._
import scala.xml._

case class AddPackage(pack: AvailablePackage)
extends Message {
  override def toXML = 
    Elem(null, "addpackage", Null, TopScope,
	 pack.toXML)
}

object AddPackageUtil {
  def fromXML(node:Node) = {
    val pack = AvailablePackageUtil.fromXML((node \ "availablePackage")(0))
    new AddPackage(pack)
  }
}
