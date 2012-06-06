import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer

object ScalaDistroFinder {
  val jenkinsUrl = SettingKey[String]("typesafe-build-server-url")
  val scalaDistJenkinsUrl = SettingKey[String]("scala-dist-jenkins-url")
  val scalaDistZipFile = TaskKey[File]("scala-dist-zip-file")

  val scalaDistZipLocation = SettingKey[File]("scala-dist-zip-location")  
  val scalaDistDir = TaskKey[File]("scala-dist-dir", "Resolves the Scala distribution and opens it into the desired location.")

  val scalaDistChecked = AttributeKey[Boolean]("scala-dist-location-checked")


  def scalaDistInstance: Setting[_] = 
    scalaInstance <<=  (scalaDistDir, appConfiguration) map { (dir, app) => 
      val jars = (dir / "lib" ** "*.jar").get
      val lib = jars find (_.getName == "scala-library.jar") getOrElse sys.error("Could not find scala library in distro.")
      val comp = jars find (_.getName == "scala-compiler.jar") getOrElse sys.error("Could not find scala library in distro.")
      val extraJars = jars filterNot { f => (f.getName == "scala-library.jar") || (f.getName == "scala-compiler.jar") }
      ScalaInstance(lib, comp, app.provider.scalaProvider.launcher, extraJars:_*)
    }

  def findDistroSettings: Seq[Setting[_]] = Seq(
    jenkinsUrl := "http://10.0.1.211/",
    scalaDistJenkinsUrl <<= jenkinsUrl apply (_ + "job/scala-release-main/ws/dists/latest/*zip*/latest.zip"),
    scalaDistZipFile <<= (scalaDistJenkinsUrl, target) map findOrDownloadZipFile,
    commands += distCheckCommand,
    onLoad in Global <<= (onLoad in Global) ?? idFun[State],
    onLoad in Global <<= (onLoad in Global) apply ( _ andThen ("scala-dist-check" :: _)),
    scalaDistInstance
  )


  def extractDistroSettings: Seq[Setting[_]] = Seq(
     // Pulling latest distro code. TODO - something useful....
    scalaDistZipLocation <<= target apply (_ / "dist"),
    scalaDistDir <<= (version, scalaDistZipFile, scalaDistZipLocation) map extractAndCleanScalaDistro
  )

  def allSettings: Seq[Setting[_]] = findDistroSettings ++ extractDistroSettings



  def distCheckCommand = Command.command("scala-dist-check") { (state: State) =>
    if(state.get(scalaDistChecked) getOrElse false) state
    else {
      // TODO - Don't run if already run.
      val extracted = Project.extract(state)
      import extracted._ 
      val targetdir = extracted get target
      val scalaDistZip = targetdir / "tmp" / "scala-dist.zip"
      val downloadUrl = extracted get scalaDistJenkinsUrl
      if(!scalaDistZip.exists) {
        System.err.println("")
        System.err.println("[error]: Could not find: " + scalaDistZip.getAbsolutePath)
        System.err.println("")
        System.err.println("\tYou can build a scala-dist.zip from a scala project, or this build")
        System.err.println("\twill attempt to download it from the latest jenkins build:")
        System.err.println("\t" + downloadUrl)
        System.err.println()
      }
      Project.setProject(session, structure, state).put(scalaDistChecked, true)
    }
  }

  def findOrDownloadZipFile(uri: String, dir: File): File = {
    // TODO - Look in the directory for any zip file?
    val file = dir / "tmp" / "scala-dist.zip"
    // Only create if it doesn't exist.   Allow users not to rely on hudson to test the build.
    if (!file.exists) {
      IO.touch(file)
      val writer = new java.io.BufferedOutputStream(new java.io.FileOutputStream(file))
      import dispatch._
      try Http(url(uri) >>> writer)
      finally writer.close()
    }
    file
  }

  def cleanScalaDistro(dir: File): Unit =
    for {
     f <- (dir ** "*.bat").get
    } Process(Seq("unix2dos", f.getAbsolutePath), None).! match {
      case 0 => ()
      case n => sys.error("Could not unix2dos: " + f.getAbsolutePath + ".  Exit code: " + n)
    }

  def extractAndCleanScalaDistro(version: String, zip: File, dir: File): File = {
    if(!dir.exists) dir.mkdirs()
    val marker = dir / "dist.exploded"
    if(!marker.exists) {
      // Unzip distro to local filesystem.
      IO.unzip(zip, dir)   
      // TODO - Fix cleaning so it works on windows
      if(!(System.getProperty("os.name").toLowerCase contains "windows")) {
        cleanScalaDistro(dir)
      }
      IO.touch(marker)
    }
    IO listFiles dir  find (_.isDirectory) getOrElse error("could not find scala distro from " + zip.getAbsolutePath)
  }
}
