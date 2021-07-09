import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.MappingsHelper._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.useNativeZip
import com.typesafe.sbt.packager.Keys._

import com.amazonaws.services.s3.model.PutObjectResult

// can't call it Universal -- that's taken by the packager
object ScalaDist {
  val upload=TaskKey[Seq[PutObjectResult]]("s3-upload","Uploads files to an S3 bucket.")

  def createMappingsWith(deps: Seq[(sbt.librarymanagement.ConfigRef, ModuleID, Artifact, File)],
                         distMappingGen: (ModuleID, Artifact, File) => Seq[(File, String)]): Seq[(File, String)] =
    deps flatMap {
      case (configRef, id, artifact, file)
        if configRef.name == ScalaDistConfigName && id.configurations.contains("runtime") =>
        distMappingGen(id, artifact, file)
      case _ => Seq()
    }

  // used to make s3-upload upload the file produced by fileTask to the path scala/$version/${file.name}
  private def uploadMapping(fileTask: TaskKey[File]) = Def.task {
    val file = fileTask.value
    file -> s"scala/${version.value}/${file.getName}"
  }

  // make it so that s3-upload will upload the msi when we're running on windows, and everything else when we're on linux
  // s3-upload thus depends on the package tasks listed below
  def platformSettings =
    if (sys.props("os.name").toLowerCase(java.util.Locale.US) contains "windows")
      Wix.settings :+ (upload / mappings += uploadMapping(Windows / packageBin).value)
    else Unix.settings ++ Seq(
      upload / mappings += uploadMapping(Universal / packageBin).value,
      upload / mappings += uploadMapping(Universal / packageZipTarball).value,
      upload / mappings += uploadMapping(UniversalDocs / packageBin).value,
      upload / mappings += uploadMapping(UniversalDocs / packageZipTarball).value,
      upload / mappings += uploadMapping(UniversalDocs / packageXzTarball).value,
      upload / mappings += uploadMapping(Rpm / packageBin).value,
      // Debian needs special handling because the value sbt-native-packager
      // gives us for `Debian / packageBin` (coming from the archiveFilename
      // method) includes the debian version and arch information,
      // which we historically have not included.  I don't see a way to
      // override the filename on disk, so we re-map at upload time
      upload / mappings += Def.task {
        (Debian / packageBin).value ->
          s"scala/${version.value}/${(Debian / name).value}-${version.value}.deb"
      }.value
    )

  def settings: Seq[Setting[_]] =
    useNativeZip ++ // use native zip to preserve +x permission on scripts
    Seq(
      name                := "scala",
      maintainer          := "LAMP/EPFL and Lightbend, Inc. <scala-internals@googlegroups.com>",
      packageSummary      := "Scala Programming Language Distribution", // this will be spliced into the middle of a sentence --> no period (it also determines sort order, so, no "The" in front)
      packageDescription  := "Have the best of both worlds. Construct elegant class hierarchies for maximum code reuse and extensibility, implement their behavior using higher-order functions. Or anything in-between.",
      crossPaths          := false,

      ivyConfigurations   += ScalaDistConfig,
      libraryDependencies += scalaDistDep(version.value, "runtime"),

      // create lib directory by resolving scala-dist's dependencies
      // to populate the rest of the distribution, explode scala-dist artifact itself
      Universal / mappings ++= createMappingsWith(update.value.toSeq, universalMappings),

      // work around regression in sbt-native-packager 1.0.5 where
      // these tasks invoke `tar` without any flags at all.  the issue
      // was fixed in 1.1.0, so this could be revisited when we upgrade
      UniversalDocs / packageZipTarball / universalArchiveOptions := Seq("--force-local", "-pcvf"),
      UniversalDocs / packageXzTarball  / universalArchiveOptions := Seq("--force-local", "-pcvf")

    )

  // private lazy val onWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")
  // only used for small batch files, normalize line endings in-place
  // private def toDosInPlace(f: File) = IO.writeLines(file, IO.readLines(file))

  private lazy val ScalaDistConfigName = "scala-dist"
  private lazy val ScalaDistConfig = config(ScalaDistConfigName)
  // segregate scala-dist's compile dependencies into the scala-dist config
  private def scalaDistDep(v: String, config: String): ModuleID =
    "org.scala-lang" % "scala-dist" % v % s"${ScalaDistConfigName}; ${ScalaDistConfigName}->${config}"

  // map module to the corresponding file mappings (unzipping scala-dist in the process)
  private def universalMappings(id: ModuleID, artifact: Artifact, file: File): Seq[(File, String)] = id.name match {
    // scala-dist: explode (drop META-INF/)
    case "scala-dist" =>
      val tmpdir = IO.createTemporaryDirectory
      IO.unzip(file, tmpdir)

      // create mappings from the unzip scala-dist zip
      contentOf(tmpdir) filter {
        case (_, dest) => !(dest.endsWith("MANIFEST.MF") || dest.endsWith("META-INF"))
      } map {
        // make unix scripts executable (heuristically...)
        case (file, dest) if (dest startsWith "bin/") && !(dest endsWith ".bat") =>
          file.setExecutable(true, true)
          file -> dest
        case mapping => mapping
      }

    // core jars: use simple name for backwards compat
    case "scala-library" | "scala-reflect" | "scala-compiler" =>
      Seq(file -> s"lib/${id.name}.jar")

    // other artifacts: qualify with version etc (they're also binary cross versioned)
    // TODO: prefix with ${id.organization} when it doesn't start with org.scala-lang/akka/com.typesafe?
    case _ =>
      Seq(file -> s"lib/${id.name}-${id.revision}.jar")
  }
}
