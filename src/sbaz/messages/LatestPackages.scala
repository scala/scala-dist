/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.messages

import sbaz._
import scala.xml._

/**
 *  A message from the server to the client listing all packages
 *  currently listed on the server.
 */
case class LatestPackages(packages: AvailableList) extends Message {
  override def toXML: Node = 
<latestpackages>
  { packages.toOldXML }
</latestpackages> ;
//COMPAT: the above toXML uses the old format so that people can upgrade
//        from old managed directories....
}


object LatestPackagesUtil {
  def fromXML(node: Node) = {
    val packsXML = (node \ "packageset")(0)
    val packs = AvailableListUtil.fromXML(packsXML)
    new LatestPackages(packs)
  }
}
