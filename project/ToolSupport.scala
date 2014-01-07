/*

lazy val emacs = (
  Project("emacs-support", file("tool-support/src/emacs/"))
  settings(packagerSettings:_*)
  settings(Versioning.versionSettings(scalaDistVersion):_*)
  settings(
     name := "scala-emacs-mode",
     wixConfig := <wix/>,
     maintainer := "Scala Community <scala-tools@googlegroups.com>",
     packageSummary := "Scala language emacs mode",
     packageDescription := """An emacs mode for the scala language.""",
     rpmRelease := "1",
     rpmVendor := "typesafe",
     rpmUrl := Some("http://github.com/scala/scala-dist"),
     rpmLicense := Some("BSD"),
     mappings in Universal <++= baseDirectory map { dir =>
       val targetDir = dir / "target"
       val targetFiles = new FileFilter {
         def accept(f: File): Boolean = f.getAbsolutePath startsWith targetDir.getAbsolutePath
       }
       (dir ** (AllPassFilter -- targetFiles) --- dir) x relativeTo(dir)
     }
  )
)

lazy val gedit = (
  Project("gedit-support", file("tool-support/src/gedit/"))
  settings(packagerSettings:_*)
  settings(Versioning.versionSettings(scalaDistVersion):_*)
  settings(
     name := "scala-gedit-highlighting",
     wixConfig := <wix/>,
     maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
     packageSummary := "Scala syntax highlighting for Gedit3",
     packageDescription := """A syntax highlighting plugin for Gedit3.""",
     linuxPackageMappings <+= baseDirectory map { dir =>
        val plugin = dir / "scala.lang"
       (packageMapping(plugin -> "/usr/share/gtksourceview-3.0/language-specs/scala.lang") withPerms "0644")
     },
     rpmRelease := "1",
     rpmVendor := "typesafe",
     rpmUrl := Some("http://github.com/scala/scala-dist"),
     rpmLicense := Some("BSD"),
     debianPackageDependencies += "gedit",
     mappings in Universal <++= baseDirectory map { dir =>
       Seq(
         dir / "README" -> "README",
         dir / "scala.lang" -> "scala.lang"
       )
     },
     distributionFiles in Linux <+= packageBin in Debian
  )
)

val toolSupport = (
  Project("tool-support", file("tool-support"))
  settings(packagerSettings:_*)
  settings(Versioning.versionSettings(scalaDistVersion):_*)
  settings(
    name <<= version apply ("scala-tool-support-" + _),
    // TODO - Disable windows,rpm,deb.  We just want an aggregate zip/tgz file.
    wixConfig := <wix/>,
    maintainer := "",
    packageSummary := "",
    packageDescription := """""",
    rpmRelease := "1",
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/scala/scala-dist"),
    rpmLicense := Some("BSD"),
    addToolSupportToZip(gedit),
    addToolSupportToZip(emacs)
  )
  settings(addUniversalToDistro:_*)
)

def addToolSupportToZip(project: Project): Setting[_] =
    mappings in Universal <++= (mappings in Universal in project, name in project) map { (m, n) =>
      for {
        (file, name) <- m
        newName = n + "/" + name
      } yield file -> newName
    }

*/