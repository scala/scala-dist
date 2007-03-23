/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.keys

import sbaz.messages._
import sbaz.keys.msgpatt._
import scala.xml._

/** A class of messages that can be authorized by a Key. */
abstract class MessagePattern {
  /** Test a particular message */
  def matches(msg: Message): Boolean
  
  def toXML: Node
}

object MessagePattern {
  def fromXML(xml: Node): MessagePattern = {
    xml match {
      case xml:Elem =>
        xml.label match {
          case "edit" => EditUtil.fromXML(xml)
          case "read" => Read
          case "editkeys" => EditKeys
        }
      case _ => throw new XMLFormatError(xml)
    }
  }
}
