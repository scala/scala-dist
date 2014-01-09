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
    name in UniversalDocs := s"scala-docs-${version.value}",
    // libraryDependencies += scalaDistDep(version.value, "javadoc"), // seems not to be necessary
    // need updateClassifiers to get javadoc jars
    mappings in UniversalDocs ++= createMappingsWith(updateClassifiers.value.toSeq, universalDocsMappings)
  )

  private def universalDocsMappings(id: ModuleID, artifact: Artifact, file: File): Seq[(File, String)] = {
    def includeJar = (file -> s"api/jars/${id.name}-${id.revision}-javadoc.jar")
    artifact match {
      case Artifact("scala-library" | "scala-reflect" | "scala-compiler", "doc", _, _, _, _, _) =>
        val tmpdir = IO.createTemporaryDirectory
        IO.unzip(file, tmpdir)
        // IO.listFiles(tmpdir) does not recurse, use ** with glob "*" to find all files
        val exploded = (PathFinder(IO.listFiles(tmpdir)) ** "*").get flatMap { file =>
          val relative = IO.relativize(tmpdir, file).get // .get is safe because we just unzipped under tmpdir

          Seq(file -> s"api/${id.name}/$relative")
        }
        includeJar +: exploded
      case Artifact(_, "doc", _, _, _, _, _) =>
        Seq(includeJar)
      case _ => Seq()
    }
  }
}

