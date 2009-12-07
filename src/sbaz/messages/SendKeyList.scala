/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.messages

/**
 * Request the list of keys known to the server.
 */
case object SendKeyList extends AbstractKeyMessage {
  def toXML = <sendkeylist/>
}
