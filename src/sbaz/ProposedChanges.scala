package sbaz

object ProposedChanges {
  abstract sealed class ProposedChange {
    def apply(set: PackageSet): PackageSet
  }
  case class Addition(pack: Package) extends ProposedChange {
    def apply(set: PackageSet) =
      set + pack
  }
  
  case class Removal(pack: PackageSpec) extends ProposedChange {
    def apply(set: PackageSet) =
      set.withoutSpec(pack)
  }
}
