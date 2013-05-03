import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer

trait ScalaDistroDeps {
  val scalaDistInstance: TaskKey[ScalaInstance]
  val scalaDistDir: SettingKey[File]
  val scalaDistVersion: SettingKey[String]
}

object ScalaDistroFinder {
  // TODO - Use Ivy or something to pull these in...
  def modules = Map(
    "lib/typesafe-config.jar"        -> "https://oss.sonatype.org/content/repositories/releases/com/typesafe/config/1.0.0/config-1.0.0.jar",
    "lib/akka-actors.jar"            -> "https://oss.sonatype.org/content/repositories/releases/com/typesafe/akka/akka-actor_2.10/2.1.0/akka-actor_2.10-2.1.0.jar",
    "lib/scala-actors-migration.jar" -> "https://oss.sonatype.org/content/repositories/releases/org/scala-lang/scala-actors-migration_2.10/1.0.0/scala-actors-migration_2.10-1.0.0.jar"
  )

  val scalaDistDir     = SettingKey[File]("scala-dist-dir", "Resolves the Scala distribution and opens it into the desired location.")
  val scalaDistVersion = SettingKey[String]("scala-dist-version")

  def scalaDistInstance: Setting[_] =
    scalaInstance <<=  (scalaDistDir, appConfiguration) map { (scalaDistDir, app) =>
      val jars      = (scalaDistDir / "lib" ** "*.jar").get
      val lib       = jars find (_.getName == "scala-library.jar")  getOrElse sys.error("Could not find scala library in distro.")
      val comp      = jars find (_.getName == "scala-compiler.jar") getOrElse sys.error("Could not find scala library in distro.")
      val extraJars = jars filterNot { f => (f.getName == "scala-library.jar") || (f.getName == "scala-compiler.jar") }
      ScalaInstance(lib, comp, app.provider.scalaProvider.launcher, extraJars:_*)
    }

  def useDistroSettings: Seq[Setting[_]] = Seq(
    scalaDistDir <<= baseDirectory apply (_ / "target" / "dist" / "latest"),
    scalaDistInstance,
    scalaDistVersion <<= (scalaDistDir, version) apply { (scalaDistDir, v) =>
      Versioning.getScalaVersionOr(scalaDistDir / "lib" / "scala-library.jar", v)
    },
    commands += distFinishCommand
  )

  def distFinishCommand = Command.command("scala-dist-finish") { (state: State) =>
    val extracted = Project.extract(state)
    finishScalaDistro(extracted get scalaDistDir)
    state
  }

  def allSettings: Seq[Setting[_]]  = useDistroSettings
  def rootSettings: Seq[Setting[_]] = useDistroSettings

  def finishScalaDistro(scalaDistDir: File): Unit = {
    removeScalacheck(scalaDistDir)
    obtainModules(scalaDistDir)

    if(!(System.getProperty("os.name").toLowerCase contains "windows"))
      fixBatFiles(scalaDistDir)
  }

  def removeScalacheck(scalaDistDir: File): Unit =
    for {
       f <- (scalaDistDir / "lib" ** "*.jar").get
       _ = println("Checking: " + f.getName)
       if f.getName contains "scalacheck"
       _ = println("Removing " + f.getAbsolutePath)
    } IO.delete(f)

  def obtainModules(scalaDistDir: File): Unit = {
    for {
      (path, uri) <- modules
      val file = scalaDistDir / path
    } download(uri, file)
  }

  def download(uri: String, to: File): File = {
    if(!to.exists) {
      IO.touch(to)
      val writer = new java.io.BufferedOutputStream(new java.io.FileOutputStream(to))
      import dispatch._
      try Http(url(uri) >>> writer)
      finally writer.close()
    }
    to
  }

  def fixBatFiles(scalaDistDir: File): Unit =
    for {
     f <- (scalaDistDir ** "*.bat").get
    } Process(Seq("todos", f.getAbsolutePath), None).! match {
      case 0 => ()
      case n => sys.error("Could not execute todos: " + f.getAbsolutePath + ".  Exit code: " + n)
    }
}
