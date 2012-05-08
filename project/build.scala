import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer


object ScalaDistBuild extends {
  override val root = Project("root", file(".")) settings(ScalaDistroFinder.allSettings:_*)
  override val scalaDistDir: TaskKey[File] = ScalaDistroFinder.scalaDistDir in root
} with Build 
  with ScalaInstallerBuild
  with ExamplesBuild {
  override def projects = Seq(root, examples, installer)
}
