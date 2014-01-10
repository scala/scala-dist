import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.{LinuxPackageMapping => pkgMap, LinuxSymlink}

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

    (packageBin in Rpm) <<= ((packageBin in Rpm)
      dependsOn (stage in Universal)
      dependsOn (stage in UniversalDocs)),

    (packageBin in Debian) <<= ((packageBin in Debian)
      dependsOn (stage in Universal)
      dependsOn (stage in UniversalDocs)),

    // symlinks for s"/usr/bin/$script" --> s"${installTargetUnix.value}/bin/$script"
    linuxPackageSymlinks ++= (
      (mappings in Universal).value collect {
        case (file, name) if (name startsWith "bin/") && !(name endsWith ".bat") =>
          LinuxSymlink("/usr/" + name, (installTargetUnix.value / name).getAbsolutePath)
      }
    ),

    linuxPackageMappings ++= {
      def home(name: String)    = (installTargetUnix.value / name).getAbsolutePath
      def docHome(name: String) = (installTargetUnixDocs.value / name).getAbsolutePath

      val m = (mappings in Universal).value

      // some mappings need special treatment (different root, perms,...)
      val (special, regular) = m partition { case (file, name) =>
        (name startsWith "bin") || (name startsWith "doc") || (name startsWith "man")
      }
      val docs = (mappings in UniversalDocs).value

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
            (((sourceDirectory in Linux).value / "copyright") -> docHome("copyright")))
          withPerms "0644").asDocs
      )
    },

    // RPM Specific
    name in Rpm    := "scala",
    rpmVendor      := "typesafe",
    rpmUrl         := Some("http://github.com/scala/scala"),
    rpmLicense     := Some("BSD"),
    rpmGroup       := Some("Development/Languages"),

    // This hack lets us ignore the RPM specific versioning junks.
    packageBin in Rpm := {
      val simplified = target.value / s"${(name in Rpm).value}-${version.value}.rpm"

      val rpm = (packageBin in Rpm).value match {
        case reported if reported.exists => reported
        case _ => // hack on top of hack because RpmHelper.buildRpm is broken -- `spec.meta.arch` doesn't necessarily match the arch `rpmbuild` decided on
          (PathFinder(IO.listFiles((target in Rpm).value)) ** "*.rpm").get.find(file =>
            file.getName contains (name in Rpm).value).get
      }

      IO.copyFile(rpm, simplified)
      simplified
    },

    // Debian Specific
    name in Debian    := "scala",
    debianPackageDependencies += "openjdk-6-jre | java6-runtime",
    debianPackageDependencies += "libjansi-java",

    linuxPackageMappings in Debian += (packageMapping(
        (sourceDirectory.value / "debian" / "changelog") -> "/usr/share/doc/scala/changelog.gz"
      ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs(),

    // Hack so we use regular version, rather than debian version.
    target in Debian := target.value / s"${(name in Debian).value}-${version.value}"
  )
}
