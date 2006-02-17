package sbaz
import java.io.File
import java.util.zip.ZipFile
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
    def pack = {
      val zip = new ZipFile(file)
      val ent = zip.getEntry("meta/description")
      if(ent == null)
        throw new Error("malformed package file: meta/description is missing")
    

      val inBytes = zip.getInputStream(ent)
      val packXML = XML.load(inBytes)
      inBytes.close()
      zip.close()
	
      PackageUtil.fromXML(packXML)
    }
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
