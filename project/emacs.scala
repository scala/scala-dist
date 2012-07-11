import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer
import DistroKeys._

trait EmacsBuild extends Build with ScalaDistroDeps {
  lazy val emacs = (
    Project("emacs-support", file("tool-support/src/emacs/")) 
    settings(packagerSettings:_*)
    settings(Versioning.versionSettings(scalaDistVersion):_*)
    settings(
       name := "scala-emacs-mode",
       wixConfig := <wix/>,
       maintainer := "Scala Community <scala-tools@googlegroups.com>",
       packageSummary := "Scala language emacs mode",
       packageDescription := """An emacs mode for the scala language.""",
       rpmRelease := "1",
       rpmVendor := "typesafe",
       rpmUrl := Some("http://github.com/scala/scala-dist"),
       rpmLicense := Some("BSD"),
       mappings in Universal <++= baseDirectory map { dir => 
         val targetDir = dir / "target"
         val targetFiles = new FileFilter { 
           def accept(f: File): Boolean = f.getAbsolutePath startsWith targetDir.getAbsolutePath
         }
         (dir ** (AllPassFilter -- targetFiles) --- dir) x relativeTo(dir)
       }
    )
  )
} 
