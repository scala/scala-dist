/*
object Unix {
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

  def settings = Seq (
    version in Rpm <<= (version in Windows) apply getRpmVersion,
    rpmRelease <<= (version in Windows) apply getRpmBuildNumber,
    version in Debian <<= (version in Windows) apply getDebianVersion
// distributionFiles in Linux <+= packageBin in Debian
// distributionFiles in Linux <+= packageBin in Rpm
// distributionFiles in Linux <+= packageXzTarball in UniversalDocs


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
        fixedName = if(name == "jline.jar") "scala-jline.jar" else name
      } yield file -> ("/usr/share/java/" + fixedName)
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
      if(!patcheddir.exists) patcheddir.mkdirs()
      val binFiles = (scriptdir ** ("*" -- "*.bat") --- scriptdir) x { f => IO.relativize(scriptdir, f) }
      val scripts = for {
        (file, name) <- binFiles
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
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/scala/scala"),
    rpmLicense := Some("BSD"),
    // This hack lets us ignore the RPM specific versioning junks.
    packageBin in Rpm <<= (packageBin in Rpm, target, name in Rpm, version) map { (p, t, n, v) =>
      val rpm = t / (n + "-" + v + ".rpm")
      IO.copyFile(p, rpm)
      rpm
    },

    // Debian Specific
    name in Debian := "scala",
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
    }
  )
}
*/