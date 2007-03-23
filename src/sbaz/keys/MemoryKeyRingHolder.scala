/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.keys

/** An in-memory keyring holder.  It holds all keys in
  * memory and does nothing when requested to save.
  */
class MemoryKeyRingHolder extends Object with KeyRingHolder {
  val keyring = new KeyRing
  def save = ()
}
