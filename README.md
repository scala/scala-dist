# Scala 2.13 Distribution

This project morphs the `"org.scala-lang" % "scala-dist" % $version` maven artifact
into a Scala `$version` distribution (zip, tar.gz, deb, rpm, and msi).

To build a distribution, run:

  * `Universal/packageBin` - Builds the universal zip installer
  * `UniversalDocs/packageBin` - Builds the universal documentation zip
  * `Debian/packageBin`  - Builds the Debian DEB file.  *requires dpkg-deb*
  * `Rpm/packageBin`     - Builds the yum RPM file.  *requires rpmbuild*
  * `Windows/packageBin` - Builds the Windows MSI.  *requires WiX 3.6*

Alternatively, the `s3Upload` task's mappings are configured based on the platform
the installer is running on.  On Windows, it builds the MSI; on another platform,
it'll create and upload the other packages in the above list. (Use `s3Upload/mappings` for a dry-run.)

The version of Scala to package is derived from the most recent git tag,
or you can specify it using `-Dproject.version`.

## Windows VM

  - install Windows 7 professional N 64-bit, ensure network access to GitHub
  - install Oracle Java 6 JDK
  - install WiX v3.6 (reboot!)
  - download sbt launcher from xsbt.org to `c:\users\jenkins\Downloads`
  - install Git
  - configure the Jenkins master's tool locations for
     - HOME
     - JDK path
     - Git (path of git.exe)
     - sbt launch jar

## Contributing

Please sign the [CLA](https://contribute.akka.io/contribute/cla/scala).

The branching structure mimics that of [scala/scala](https://github.com/scala/scala);
there are branches for 2.12.x and 2.13.x.
