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
