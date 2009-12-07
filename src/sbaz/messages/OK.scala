/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.messages

import sbaz._
import scala.xml._

// A message claiming that the previous request succeeded.
case class OK() extends Message {
  override def toXML =  <ok/> ;
}
