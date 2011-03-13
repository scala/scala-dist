/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

// an error in the format of some string
class FormatError(msg: String)
extends Exception(msg) {
  def this() = this("Format Error")
}
