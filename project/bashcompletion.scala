import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer



object BashCompletion {

  def makeProject(root: Project, distDir: TaskKey[File]) = (
    Project("bash-completion", file("bash-completion"))
    settings(
      scalaInstance <<= (distDir in root, appConfiguration) map { (dir, app) =>
        val launcher = app.provider.scalaProvider.launcher        
        ScalaInstance(dir, launcher)
      },
      unmanagedJars in Compile <+= distDir in root map (_ / "lib/scala-compiler.jar")
    )
  )
}
