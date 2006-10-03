package sbaz
import java.io.File
import scala.xml._

object ProposedChanges {
  abstract sealed class ProposedChange {
    def apply(set: PackageSet): PackageSet
  }
  
  abstract class Addition extends ProposedChange {
    def pack: Package

    def apply(set: PackageSet) =
      set + pack
  }
  
  case class AdditionFromFile(file: File) extends Addition {
    val packfile = new PackageFile(file)
    def pack = packfile.pack
  }
  
  case class AdditionFromNet(avail: AvailablePackage) extends Addition {
    def pack = avail.pack
    def link = avail.link
  }
  
  case class Removal(pack: PackageSpec) extends ProposedChange {
    def apply(set: PackageSet) =
      set.withoutSpec(pack)
  }
}
