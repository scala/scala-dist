import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer


trait Documentation extends Build 
  with ScalaInstallerBuild {
  import Documentation._

  val documentation = (
    Project("documentation", file("documentation"))
    settings(
      name := "scala-documentation",
      version <<= version in installer,
      // TODO - Disable windows,rpm,deb.  We just want an aggregate zip/tgz file.
      wixConfig := <wix/>,
      maintainer := "",
      packageSummary := "",
      packageDescription := """""",
      rpmRelease := "1",
      rpmVendor := "typesafe",
      rpmUrl := Some("http://github.com/scala/scala-dist"),
      rpmLicense := Some("BSD"),
      makeReference <<= (baseDirectory, target, streams) map makeLatex("ScalaReference")
    )
  )

}

object Documentation {


  val makeReference = TaskKey[File]("make-scala-reference")

  def makeLatex(name: String)(baseDirectory: File, target: File, s: TaskStreams): File =
    latex2pdf(baseDirectory / "src" / "reference", baseDirectory / "lib", target, name, s.log)

  def latex2pdf(dir: File, libdir: File, target: File, name: String, log: Logger): File = {
    Process(
      Seq("latexmk", "-g", "-pdf", (dir / name).getAbsolutePath), 
      Some(target), 
      "TEXINPUTS" -> "%s:%s:".format(libdir.getAbsolutePath, dir.getAbsolutePath),
      "BIBINPUTS"  -> "%s:".format(dir.getAbsolutePath)
    ) ! log match {
      case 0 => ()
      case n => sys.error("Trouble running latexmk.  Exit code: " + n)
    }
    dir / (target + ".ps")
  }

  def pdf2ps(pdf: File, target: File, log: Logger): File = {
    // TODO - better name.
    val ps = target / (pdf.getName + ".ps")
    Process(
      Seq("pdf2ps", pdf.getAbsolutePath, ps.getAbsolutePath),
      Some(target)
    ) ! log match {
      case 0 => ()
      case n => sys.error("Trouble running pdf2ps.  Exit code: " + n)
    }
    ps
  }
      
}
