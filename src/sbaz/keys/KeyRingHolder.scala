/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.keys

trait KeyRingHolder {
  val keyring: KeyRing
  def save: Unit
}
