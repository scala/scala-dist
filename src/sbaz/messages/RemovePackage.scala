/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.messages

import sbaz._
import scala.xml._

case class RemovePackage(spec:PackageSpec)
extends Message {
  override def toXML = 
    Elem(null, "removepackage", Null, TopScope,
	 spec.toXML)

}


object RemovePackageUtil {
  def fromXML(node: Node) = {
    val spec = PackageSpecUtil.fromXML((node \ "packagespec")(0))
    new RemovePackage(spec)
  }
}
