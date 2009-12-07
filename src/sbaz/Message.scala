/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import scala.xml.{Elem, Node}

import sbaz.messages._ 
import sbaz.keys.Key

// A message that can be sent across a MessageStream.
abstract class Message {
  /** Convert a message to XML.  Convert it back using
    * MessageUtil.fromXML */
  def toXML: Node 

  /** The list of keys supplied along with this message */
  def authKeys: List[Key] = Nil
  
  /** The bare message, without any keys attached.  Useful
    * for pattern matching.
    */
  def sansKeys: Message = this
  
  /** This message along with the supplied keys. */
  def withKeys(newkeys: List[Key]): Message = {
    if (newkeys.isEmpty)
      this
    else
      MessageWithKeys(newkeys:::authKeys, sansKeys)
  }
}  


// XXX naming it Message crashes the compiler
object MessageUtil {
  def fromXML(node: Node): Message = node match {
    case node:Elem =>
      node.label match {
        case "addpackage" => AddPackageUtil.fromXML(node)
        case "removepackage" => RemovePackageUtil.fromXML(node)
        case "sendpackagelist" => SendPackageListUtil.fromXML(node)

        case "latestpackages" => LatestPackagesUtil.fromXML(node)
        case "ok" => OK()
        case "notok" => NotOKUtil.fromXML(node)
    
        case "keycreate" => KeyCreateUtil.fromXML(node)
        case "keycreated" => KeyCreatedUtil.fromXML(node)
        case "keylist" => KeyListUtil.fromXML(node)
        case "keyrevoke" => KeyRevokeUtil.fromXML(node)
        case "sendkeylist" => SendKeyList
        case "messagewithkeys" => MessageWithKeysUtil.fromXML(node)

        case _ => throw new Error("not a valid Message: " + node)
      }

    // XXX ParseError
    case _ => throw new Error("not a valid Message")
  }
}
