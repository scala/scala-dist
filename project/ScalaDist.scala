import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.MappingsHelper._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.useNativeZip
import com.typesafe.sbt.packager.Keys._

// can't call it Universal -- that's taken by the packager
object ScalaDist {
  val ghUpload = TaskKey[Seq[Unit]]("ghUpload","Uploads files to GitHub Releases.")

  def createMappingsWith(deps: Seq[(sbt.librarymanagement.ConfigRef, ModuleID, Artifact, File)],
                         distMappingGen: (ModuleID, Artifact, File) => Seq[(File, String)]): Seq[(File, String)] =
    deps flatMap {
      case (configRef, id, artifact, file)
        if configRef.name == ScalaDistConfigName && id.configurations.contains("runtime") =>
        distMappingGen(id, artifact, file)
      case _ => Seq()
    }

  // uploads the file produced by fileTask under its own name
  private def uploadMapping(fileTask: TaskKey[File]) = Def.task {
    val file = fileTask.value
    file -> file.getName
  }

  // on windows, upload the msi; elsewhere, upload all other packages
  def platformSettings =
    if (sys.props("os.name").toLowerCase(java.util.Locale.US) contains "windows")
      Wix.settings :+ (ghUpload / mappings += uploadMapping(Windows / packageBin).value)
    else Unix.settings ++ Seq(
      ghUpload / mappings += uploadMapping(Universal / packageBin).value,
      ghUpload / mappings += uploadMapping(Universal / packageZipTarball).value,
      ghUpload / mappings += uploadMapping(UniversalDocs / packageBin).value,
      ghUpload / mappings += uploadMapping(UniversalDocs / packageZipTarball).value,
      ghUpload / mappings += uploadMapping(UniversalDocs / packageXzTarball).value,
      ghUpload / mappings += uploadMapping(Rpm / packageBin).value,
      // sbt-native-packager names the file like scala_2.13.18-400_all.deb (scala/scala-dev#921)
      ghUpload / mappings += Def.task {
        (Debian / packageBin).value -> s"${(Debian / name).value}-${version.value}.deb"
      }.value
    )

  def settings: Seq[Setting[_]] =
    useNativeZip ++ // use native zip to preserve +x permission on scripts
    Seq(
      name                := "scala",
      maintainer          := "LAMP/EPFL and Akka",
      packageSummary      := "Scala Programming Language Distribution", // this will be spliced into the middle of a sentence --> no period (it also determines sort order, so, no "The" in front)
      packageDescription  := "Have the best of both worlds. Construct elegant class hierarchies for maximum code reuse and extensibility, implement their behavior using higher-order functions. Or anything in-between.",
      crossPaths          := false,

      ivyConfigurations   += ScalaDistConfig,
      libraryDependencies += scalaDistDep(version.value, "runtime"),

      // create lib directory by resolving scala-dist's dependencies
      // to populate the rest of the distribution, explode scala-dist artifact itself
      Universal / mappings ++= createMappingsWith(update.value.toSeq, universalMappings),
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
