package sbaz
import java.io.File
import sbaz.keys._

/** A set of keys usable on a single universe.  It
  * is backed by a file within some ManagedDirectory.
  */
class KeysForUniverse(
    val keyring: KeyRing,
    val universe: SimpleUniverse, 
    val backingFile: File)
extends Object
with FileBackedObject
{
	def toXML = keyring.toXML
}
