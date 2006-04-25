package sbaz.keys

trait KeyRingHolder {
  val keyring: KeyRing
	def save: Unit
}
