/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.messages

/** An abstract class designating that a message
  * class involves an operation on the keys
  * of a bazaar server.  This is used for
  * security policies; see class <code>sbaz.keys.msgpatt.EditKeys</code>.
  */
abstract class AbstractKeyMessage extends Message
