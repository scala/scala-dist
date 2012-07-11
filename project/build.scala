import sbt._
import Keys._
import com.typesafe.packager.Keys._
import com.typesafe.packager.universal.Keys.{packageZipTarball,packageXzTarball}
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer

object DistroKeys {
  val distributionFiles = TaskKey[Seq[File]]("distribution-files", "Files used for a scala distribution")
  val makeOsDistro = TaskKey[Seq[File]]("make-os-distribution", "Aggregates all the distribution files this OS can create and places them in a local directory.")
  // Helpers
  def addDebianToDistro: Setting[_] =
    distributionFiles in Linux <+= packageBin in Debian
  def addRpmToDistro: Setting[_] =
    distributionFiles in Linux <+= packageBin in Rpm
  def addShrunkenDocsToDistro: Setting[_] =
    distributionFiles in Linux <+= packageXzTarball in UniversalDocs    
  def addMsiToDistro: Setting[_] =
    distributionFiles in Windows <+= packageMsi in Windows

  private def addZipsToDistro(c: Configuration): Seq[Setting[_]] = Seq(
    distributionFiles in Linux <+= packageBin in c,
    distributionFiles in Windows <+= packageBin in c, 
    distributionFiles in Linux <+= packageZipTarball in c
  )

  def addUniversalToDistro: Seq[Setting[_]] = addZipsToDistro(Universal)
  def addDocsToDistro: Seq[Setting[_]] = addZipsToDistro(UniversalDocs)
  def addSrcsToDistro: Seq[Setting[_]] = addZipsToDistro(UniversalSrc)
}

object ScalaDistBuild extends {
  val root = Project("root", file(".")) settings(ScalaDistroFinder.rootSettings:_*)
  override val scalaDistInstance: TaskKey[ScalaInstance] = scalaInstance in root
  override val scalaDistDir: SettingKey[File] = ScalaDistroFinder.scalaDistDir in root
  override val scalaDistVersion: SettingKey[String] = ScalaDistroFinder.scalaDistVersion in root
} with Build 
  with ScalaInstallerBuild
  with ExamplesBuild 
  with ToolSupport 
  with Documentation {

  import DistroKeys._
  override def settings = super.settings ++ Seq(distributionFiles := Seq.empty) ++ ScalaDistroFinder.allSettings

  override def projects = Seq(completeDistribution, root, examples, installer, gedit, toolSupport, documentation, emacs)

  def distroProjects = Seq(examples, installer, gedit, toolSupport, documentation)


  val completeDistribution = (
    Project("distribution", file(".")) 
    settings((inConfig(Linux)(distroSettings)):_*)
    settings((inConfig(Windows)(distroSettings)):_*)
  )

  def distroSettings: Seq[Setting[_]] = Seq(
    distributionFiles <<= (distroProjects map (distributionFiles in _)).join map (_.flatten),
    makeOsDistro <<= (distributionFiles, target in root) map { (files, dir) =>
      for { 
        f <- files
        to = dir / f.getName
      } yield {
        IO.copyFile(f, to, preserveLastModified=true)
        to
      }
    }
  )
}
