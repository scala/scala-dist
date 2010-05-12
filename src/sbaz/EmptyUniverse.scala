/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import scala.xml._

class EmptyUniverse extends Universe
{
  def retrieveAvailable() = new AvailableList(Nil)
  override def simpleUniverses = List()
  override def toXML = <emptyuniverse/>
  override def toString() = "EmptyUniverse"
}
