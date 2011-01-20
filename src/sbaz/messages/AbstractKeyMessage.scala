/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.messages

import sbaz._

/** An abstract class designating that a message
  * class involves an operation on the keys
  * of a bazaar server.  This is used for
  * security policies; see class <code>sbaz.keys.msgpatt.EditKeys</code>.
  */
abstract class AbstractKeyMessage extends Message
