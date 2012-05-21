import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer


trait ToolSupport extends Build 
  with ScalaInstallerBuild
  with ExamplesBuild 
  with GeditBuild {

  val toolSupport = (
    Project("tool-support", file("tool-support"))
    settings(packagerSettings:_*)
    settings(
      name := "scala-tool-support",
      version <<= version in installer,
      // TODO - Disable windows,rpm,deb.  We just want an aggregate zip/tgz file.
      wixConfig := <wix/>,
      maintainer := "",
      packageSummary := "",
      packageDescription := """""",
      rpmRelease := "1",
      rpmVendor := "typesafe",
      rpmUrl := Some("http://github.com/scala/scala-dist"),
      rpmLicense := Some("BSD"),
      debianPackageDependencies += "gedit",
      addToolSupportToZip(gedit)
    )
  )

  def addToolSupportToZip(project: Project): Setting[_] =
      mappings in Universal <++= (mappings in Universal in project, name in project) map { (m, n) =>
        for {
          (file, name) <- m
          newName = n + "/" + name
        } yield file -> newName
      }
}
