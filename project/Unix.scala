import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.{LinuxPackageMapping => pkgMap, LinuxSymlink}
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.packageMapping

/** Create debian & rpm packages.
 *
 * subdirs not requiring special treatment are rooted under `installTargetUnix.value`
 * docs go to `installTargetUnixDocs.value`
 * create symlinks under /usr/bin/ for all scripts in `installTargetUnix.value / "bin"`
 * make scripts in `installTargetUnix.value / "bin"` executable, drop ".bat" files
 * map man to /usr/share/man/
 * map doc to `installTargetUnixDocs.value`
 * map api to `installTargetUnixDocs.value / "api"`
 */
object Unix {
  val installTargetUnix     = SettingKey[File]("install-target-unix", "The location where we will install Scala.")
  val installTargetUnixDocs = SettingKey[File]("install-target-docs-unix", "The location where we will install the Scala docs.")

  def settings = Seq (
    installTargetUnix     := file("/usr/share/scala"),
    installTargetUnixDocs := file("/usr/share/doc/scala"),

    // symlinks for s"/usr/bin/$script" --> s"${installTargetUnix.value}/bin/$script"
    // TODO: reuse code from native packager
    linuxPackageSymlinks ++= (
      (Universal / mappings).value collect {
        case (file, name) if (name startsWith "bin/") && !(name endsWith ".bat") =>
          LinuxSymlink("/usr/" + name, (installTargetUnix.value / name).getAbsolutePath)
      }
    ),

    linuxPackageMappings ++= {
      def home(name: String)    = (installTargetUnix.value / name).getAbsolutePath
      def docHome(name: String) = (installTargetUnixDocs.value / name).getAbsolutePath

      val m = (Universal / mappings).value

      // some mappings need special treatment (different root, perms,...)
      val (special, regular) = m partition { case (file, name) =>
        (name startsWith "bin") || (name startsWith "doc") || (name startsWith "man")
      }
      val docs = (UniversalDocs / mappings).value

      Seq(
        // no special treatment needed
        (pkgMap(regular map { case (file, name) => file -> home(name) })
          withPerms "0644"),
        // make scripts in bin/ executable, drop .bat files
        (pkgMap(special collect { case (file, name) if (name startsWith "bin/") && !(name endsWith ".bat") => file -> home(name) })
          withPerms "0755"),
        // mappings for man/ --> /usr/share/man/
        (pkgMap(special collect { case (file, name) if name startsWith "man/" => file -> s"/usr/share/$name.gz"})
          withPerms "0644").gzipped.asDocs,
        // mappings for doc/ --> /usr/share/doc/scala/ and api/ --> /usr/share/doc/scala/api/
        (pkgMap(
            (special collect { case (file, name) if name startsWith "doc/" => file -> docHome(name drop 4) }) ++
            (docs    map     { case (file, name) => file -> docHome(name) }) :+
            (((Linux / sourceDirectory).value / "copyright") -> docHome("copyright")))
          withPerms "0644").asDocs
      )
    },

    // RPM Specific
    Rpm / name    := "scala",
    rpmVendor      := "lightbend",
    rpmUrl         := Some("http://github.com/scala/scala"),
    rpmLicense     := Some("BSD"),
    rpmGroup       := Some("Development/Languages"),

    // This hack lets us ignore the RPM specific versioning junks.
    Rpm / packageBin := {
      val simplified = target.value / s"${(Rpm / name).value}-${version.value}.rpm"

      val rpm = (Rpm / packageBin).value match {
        case reported if reported.exists => reported
        case _ => // hack on top of hack because RpmHelper.buildRpm is broken on Mac -- `spec.meta.arch` doesn't necessarily match the arch `rpmbuild` decided on
          (PathFinder(IO.listFiles((Rpm / target).value)) ** "*.rpm").get.find(file =>
            file.getName contains (Rpm / name).value).get
      }

      IO.copyFile(rpm, simplified)
      simplified
    },

    // Debian Specific
    Debian / name    := "scala",
    debianPackageDependencies += "java8-runtime-headless",

    Debian / linuxPackageMappings += (packageMapping(
        (sourceDirectory.value / "debian" / "changelog") -> "/usr/share/doc/scala/changelog.gz"
      ).withUser("root").withGroup("root").withPerms("0644").gzipped).asDocs()

  )
}
