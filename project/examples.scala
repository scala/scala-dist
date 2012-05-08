import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer


trait ExamplesBuild extends Build {
  val scalaDistDir: TaskKey[File]

  val examples = (
    Project("examples", file("examples")) settings(
      // TODO - Scala instance from dist dir.
      scalaInstance <<=  (scalaDistDir, appConfiguration) map { (dir, app) => ScalaInstance(dir, app.provider.scalaProvider.launcher) },
      unmanagedJars in Compile <++= (scalaDistDir) map { dir => (dir / "lib" ** "*.jar").get }
    )
  )
} 
