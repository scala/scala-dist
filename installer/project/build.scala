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

  def findDistroSettings: Seq[Setting[_]] = Seq(
    jenkinsUrl := "http://10.0.1.211/",
    scalaDistJenkinsUrl <<= jenkinsUrl apply (_ + "job/scala-release-main/ws/dists/latest/*zip*/latest.zip"),
    scalaDistZipFile <<= (scalaDistJenkinsUrl, target) map findOrDownloadZipFile
  )

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
}

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

trait Versioning {
  def getScalaVersionPropertyOr(default: String): String =
    Option(System.getProperty("scala.version")) getOrElse default

  /** This is a complicated means to convert maven version numbers into monotonically increasing windows versions. */
  def makeWindowsVersion(version: String): String = {
    val Majors = new scala.util.matching.Regex("(\\d+).(\\d+).(\\d+)(-.*)?")
    val Rcs = new scala.util.matching.Regex("(\\-\\d+)?\\-RC(\\d+)")
    val Milestones = new scala.util.matching.Regex("(\\-\\d+)?\\-M(\\d+)")
    val BuildNum = new scala.util.matching.Regex("\\-(\\d+)")

    def calculateNumberFour(buildNum: Int = 0, rc: Int = 0, milestone: Int = 0) = 
      if(rc > 0 || milestone > 0) (buildNum)*400 + rc*20  + milestone
      else (buildNum+1)*400 + rc*20  + milestone

    version match {
      case Majors(major, minor, bugfix, rest) => Option(rest) getOrElse "" match {
        case Milestones(null, num)            => major + "." + minor + "." + bugfix + "." + calculateNumberFour(0,0,num.toInt)
        case Milestones(bnum, num)            => major + "." + minor + "." + bugfix + "." + calculateNumberFour(bnum.drop(1).toInt,0,num.toInt)
        case Rcs(null, num)                   => major + "." + minor + "." + bugfix + "." + calculateNumberFour(0,num.toInt,0)
        case Rcs(bnum, num)                   => major + "." + minor + "." + bugfix + "." + calculateNumberFour(bnum.drop(1).toInt,num.toInt,0)
        case BuildNum(bnum)                   => major + "." + minor + "." + bugfix + "." + calculateNumberFour(bnum.toInt,0,0)
        case _                                => major + "." + minor + "." + bugfix + "." + calculateNumberFour(0,0,0)
      }
    }
  }

  def getRpmBuildNumber(version:String): String = version split "\\." match {
    case Array(_,_,_, b) => b
    case _ => "1"
  }

  def getRpmVersion(version:String): String = version split "\\." match {
    case Array(m,n,b,_*) => "%s.%s.%s" format (m,n,b)
    case _ => version
  }

  def getDebianVersion(version:String): String = version split "\\." match {
    case Array(m,n,b,z) => "%s.%s.%s-%s" format (m,n,b,z)
    case _ => version
  }
}



object ScalaDistro extends Build with WindowsPackaging with Versioning {
  import ScalaDistroFinder._

  val jenkinsUrl = SettingKey[String]("typesafe-build-server-url")
  val scalaDistJenkinsUrl = SettingKey[String]("scala-dist-jenkins-url")
  // TODO - Pull this zip from the latest build version of scala we wish to release.  Maybe publish into a repo somewhere....


  val scalaDistZipLocation = SettingKey[File]("scala-dist-zip-location")  
  val scalaDistDir = TaskKey[File]("scala-dist-dir", "Resolves the Scala distribution and opens it into the desired location.")

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


  val root = (Project("scala-installer", file(".")) 
              settings(packagerSettings:_*)
              settings(findDistroSettings:_*)
              settings(
    // TODO - Pull this from distro....
    version := "2.10.0",
    version <<= version apply getScalaVersionPropertyOr,

    // Pulling latest distro code. TODO - something useful....
    scalaDistZipLocation <<= target apply (_ / "dist"),
    scalaDistDir <<= (version, scalaDistZipFile, scalaDistZipLocation) map extractAndCleanScalaDistro,
    // Windows installer configuration
    name in Windows := "scala",
    version in Windows <<= version apply makeWindowsVersion,
    lightOptions ++= Seq("-ext", "WixUIExtension", "-cultures:en-us"),
    //mappings in packageMsi in Windows <++= scalaDistDir map { (dir) =>  (dir.*** --- dir) x relativeTo(dir) },
    wixConfig <<= (version in Windows, scalaDistDir, sourceDirectory in Windows) map generateWindowsXml,

    // Linux Configuration
    name in Linux := "scala",
    maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
    packageSummary := "Programming Language for the JVM",
    packageDescription := """This includes all the utilities used by the Scala programming language,
  a blended object-functional language for the JVM.""",
    // TODO - Put jline in sub-folder of /usr/share/java
    linuxPackageMappings <+= scalaDistDir map { dir =>
      val jardir = dir / "lib"
      val jars = for {
        (file, name) <- (jardir ** "*.jar") x { f => IO.relativize(jardir, f) }
      } yield file -> ("/usr/share/java/" + name)
      (packageMapping(jars:_*) withPerms "0644")
    },
    linuxPackageMappings <+= scalaDistDir map { dir =>
      val jardir = dir / "misc" / "scala-devel" / "plugins"
      val jars = for {
        (file, name) <- (jardir ** "*.jar") x { f => IO.relativize(jardir, f) }
      } yield file -> ("/usr/share/scala/plugins/" + name)
      (packageMapping(jars:_*) withPerms "0644")
    },
    // TODO - Figure out how to setup maven repo metadata for these.
    
    // TODO - Fix binaries before copying
    linuxPackageMappings <+= (scalaDistDir, sourceDirectory, streams) map { (dir, sdir, s) =>
      val patchdir = sdir / "linux" / "patch"         
      val scriptdir = dir / "bin"
      val patcheddir = dir / "patched-bin"
      
      val scripts = for {
        (file, name) <- (scriptdir ** ("*" -- "*.bat") --- scriptdir) x { f => IO.relativize(scriptdir, f) }
        patchfile = patchdir / (name + ".patch")
        patchedfile = if(patchfile.exists) patcheddir / name else file
      } yield {        
        if(!patchedfile.exists || (patchedfile.lastModified < patchfile.lastModified)) {
          IO.copyFile(file, patchedfile)
          Process(Seq("patch", "-s", "-f", patchedfile.getAbsolutePath, patchfile.getAbsolutePath)) ! s.log match {
            case 0 => ()
            case _ => sys.error("Could not apply script patch file!.")
          }
        }
        patchedfile -> ("/usr/bin/" + name)
      }
      (packageMapping(scripts:_*) withPerms "0755")
    },
    linuxPackageMappings <+= scalaDistDir map { dir =>
      val mandir = dir / "man" / "man1"
      val manpages = for {
        (file, name) <- (mandir ** "*.1") x { f => IO.relativize(mandir, f) }
      } yield file -> ("/usr/share/man/man1/" + name + ".gz")
      (packageMapping(manpages:_*) withPerms "0644" gzipped) asDocs()
    },  
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      packageMapping(
        (bd / "copyright") -> "/usr/share/doc/scala/copyright"
      ) withPerms "0644" asDocs()
    }, 
    
    // RPM SPECIFIC
    name in Rpm := "scala",
    version in Linux <<= (version in Windows) apply getRpmVersion,
    rpmRelease <<= (version in Windows) apply getRpmBuildNumber,
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/scala/scala"),
    rpmLicense := Some("BSD"),
    
    // Debian Specific
    name in Debian := "scala",
    version in Debian <<= (version in Windows) apply getDebianVersion,
    debianPackageDependencies += "openjdk-6-jre | java6-runtime",
    debianPackageDependencies += "libjansi-java",
    linuxPackageMappings in Debian <+= (sourceDirectory) map { bd =>
      (packageMapping(
        (bd / "debian/changelog") -> "/usr/share/doc/scala/changelog.gz"
      ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
    },
    // Hack so we use regular version, rather than debian version.
    target in Debian <<= (target, name in Debian, version) apply ((t,n,v) => t / (n +"-"+ v)),
    // TODO - this hack will be fixed in next version of native packager plugin...
    packageBin in Debian <<= (packageBin in Debian, target in Debian) map { case (f, t) =>
      f;
      file(t.getAbsolutePath + ".deb")
    },
    // Universal
    name in Universal <<= version apply ("scala-"+_),
    mappings in Universal <++= scalaDistDir map { dir => (dir / "bin").*** --- dir x relativeTo(dir) },
    mappings in Universal <++= scalaDistDir map { dir => (dir / "lib").*** --- dir x relativeTo(dir) },
    mappings in Universal <++= scalaDistDir map { dir => (dir / "src").*** --- dir x relativeTo(dir) },
    mappings in Universal <++= scalaDistDir map { dir => (dir / "misc").*** --- dir x relativeTo(dir) },
    mappings in Universal <++= scalaDistDir map { dir => (dir / "man").*** --- dir x relativeTo(dir) },
    mappings in Universal <++= scalaDistDir map { dir => 
      val toolshtmldir = dir / "doc" / "scala-devel-docs" / "tools"
      for( (file,path) <- (toolshtmldir).*** --- toolshtmldir x relativeTo(toolshtmldir))
      yield file -> ("doc/tools/"+path)
    },
    mappings in Universal <++= scalaDistDir map { dir => 
      Seq(dir / "doc" / "LICENSE" -> "doc/LICENSE",
          dir / "doc" / "README" -> "doc/README")
    },
    mappings in UniversalDocs <++= scalaDistDir map { dir => 
      val ddir = dir / "doc" / "scala-devel-docs" / "api"
      ddir.*** --- ddir x relativeTo(ddir)
    },
    name in UniversalDocs <<= version apply ("scala-docs-"+_)
  ))

  lazy val bashcompletion = BashCompletion.makeProject(root, scalaDistDir)
}
