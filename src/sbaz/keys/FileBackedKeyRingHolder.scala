/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.keys

import java.io.File
import sbaz.FileBackedObject
  

/** A set of keys for a universe that is backed by a file. */
class FileBackedKeyRingHolder(
    val keyring: KeyRing,
    val backingFile: File)
extends Object
with KeyRingHolder
with FileBackedObject
{
  def toXML = keyring.toXML
}
