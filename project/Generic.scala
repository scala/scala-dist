import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._

// can't call it Universal -- that's taken by the packager
object Generic {
  def createMappingsWith(deps: Seq[(String, ModuleID, Artifact, File)],
                         distMappingGen: (ModuleID, Artifact, File) => Seq[(File, String)]): Seq[(File, String)] =
    deps flatMap {
      case d@(ScalaDistConfig, id, artifact, file) => distMappingGen(id, artifact, file)
      case _ => Seq()
    }

  def settings: Seq[Setting[_]] = packagerSettings ++ Seq(
    name                := "scala",
    maintainer          := "LAMP/EPFL and Typesafe, Inc.",
    packageSummary      := "Scala",
    packageDescription  := "The Scala Programming Language.",
    crossPaths          := false,

    ivyConfigurations   += config(ScalaDistConfig),
    libraryDependencies += scalaDistDep(version.value, "runtime"),

    // create lib directory by resolving scala-dist's dependencies
    // to populate the rest of the distribution, explode scala-dist artifact itself
    mappings in Universal ++= createMappingsWith(update.value.toSeq, universalMappings)
  )

  // private lazy val onWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows")
  // only used for small batch files, normalize line endings in-place
  // private def toDosInPlace(f: File) = IO.writeLines(file, IO.readLines(file))

  private lazy val ScalaDistConfig = "scala-dist"
  // segregate scala-dist's compile dependencies into the scala-dist config
  private def scalaDistDep(v: String, config: String): ModuleID =
    "org.scala-lang" % "scala-dist" % v % s"${ScalaDistConfig}; ${ScalaDistConfig}->${config}"

  // map module to the corresponding file mappings (unzipping scala-dist in the process)
  private def universalMappings(id: ModuleID, artifact: Artifact, file: File): Seq[(File, String)] = id.name match {
    // scala-dist: explode (drop META-INF/)
    case "scala-dist" =>
      val tmpdir = IO.createTemporaryDirectory
      IO.unzip(file, tmpdir)
      // IO.listFiles(tmpdir) does not recurse, use ** with glob "*" to find all files
      (PathFinder(IO.listFiles(tmpdir)) ** "*").get flatMap { file =>
        val relative = IO.relativize(tmpdir, file).get // .get is safe because we just unzipped under tmpdir

        // files are stored in repository with platform-appropriate line endings
        // if (onWindows && (relative endsWith ".bat")) toDosInPlace(file)

        if (relative startsWith "META-INF") Seq()
        else Seq(file -> relative)
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
