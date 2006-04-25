package sbaz.keys

/** An in-memory keyring holder.  It holds all keys in
  * memory and does nothing when requested to save.
  */
class MemoryKeyRingHolder
extends Object 
with KeyRingHolder {
	val keyring = new KeyRing
  def save = ()
}
