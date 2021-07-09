import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._

/** Create mappings for UniversalDocs under the api/ directory.
 *
 * All dependencies of scala-dist under the javadoc classifier are included as
 * "api/jars/$artifactId-$version-javadoc.jar".
 *
 * The core jars are also expanded under api/scala-{library|reflect|compiler}
 *
 */
object Docs {
  import ScalaDist._

  def settings: Seq[Setting[_]] = Seq(
    UniversalDocs / packageName := s"scala-docs-${version.value}",
    // libraryDependencies += scalaDistDep(version.value, "javadoc"), // seems not to be necessary
    // need updateClassifiers to get javadoc jars
    UniversalDocs / mappings ++= createMappingsWith(updateClassifiers.value.toSeq, universalDocsMappings)
  )

  private def universalDocsMappings(id: ModuleID, artifact: Artifact, file: File): Seq[(File, String)] = {
    def includeJar = (file -> s"api/jars/${id.name}-${id.revision}-javadoc.jar")
    artifact.name match {
      case "scala-library" | "scala-reflect" | "scala-compiler" if artifact.`type` == "doc" =>
        val tmpdir = IO.createTemporaryDirectory
        IO.unzip(file, tmpdir)
        // IO.listFiles(tmpdir) does not recurse, use ** with glob "*" to find all files
        val exploded = (PathFinder(IO.listFiles(tmpdir)) ** "*").get flatMap { file =>
          val relative = IO.relativize(tmpdir, file).get // .get is safe because we just unzipped under tmpdir

          Seq(file -> s"api/${id.name}/$relative")
        }
        includeJar +: exploded
      case _ if artifact.`type` == "doc" =>
        Seq(includeJar)
      case _ => Seq()
    }
  }
}

