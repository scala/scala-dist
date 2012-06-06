import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer


trait ExamplesBuild extends Build with ScalaDistroDeps {

  val examples = (
    Project("examples", file("examples")) settings(
      // TODO - Scala instance from dist dir.
      scalaInstance <<= scalaDistInstance,
      unmanagedJars in Compile <++= (scalaDistDir) map { dir => (dir / "lib" ** "*.jar").get }
    )
  )
} 


//apply(libraryJar: File, compilerJar: File, launcher: xsbti.Launcher, extraJars: File*)
