import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer
import DistroKeys._
trait GeditBuild extends Build with ScalaDistroDeps {
  lazy val gedit = (
    Project("gedit-support", file("tool-support/src/gedit/")) 
    settings(packagerSettings:_*)
    settings(Versioning.versionSettings(scalaDistVersion):_*)
    settings(
       name := "scala-gedit-highlighting",
       wixConfig := <wix/>,
       maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
       packageSummary := "Scala syntax highlighting for Gedit3",
       packageDescription := """A syntax highlighting plugin for Gedit3.""",
       linuxPackageMappings <+= baseDirectory map { dir =>
          val plugin = dir / "scala.lang"
         (packageMapping(plugin -> "/usr/share/gtksourceview-3.0/language-specs/scala.lang") withPerms "0644")
       },
       rpmRelease := "1",
       rpmVendor := "typesafe",
       rpmUrl := Some("http://github.com/scala/scala-dist"),
       rpmLicense := Some("BSD"),
       debianPackageDependencies += "gedit",
       mappings in Universal <++= baseDirectory map { dir => 
         Seq(
           dir / "README" -> "README",
           dir / "scala.lang" -> "scala.lang"
         ) 
       },
       addDebianToDistro
    )
  )
} 
