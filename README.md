# The Scala 2.12.x Distribution

This project morphs the `"org.scala-lang" % "scala-dist" % $version` maven artifact
into a Scala `$version` distribution (zip, tar.gz, deb, rpm, and msi).

To build a distribution, run:

  * `universal:packageBin` - Builds the universal zip installer
  * `universal-docs:packageBin` - Builds the universal documentation zip
  * `debian:packageBin`  - Builds the Debian DEB file.  *requires dpkg-deb*
  * `rpm:packageBin`     - Builds the yum RPM file.  *requires rpmbuild*
  * `windows:packageBin` - Builds the Windows MSI.  *requires WiX 3.6*

Alternatively, the `s3Upload` task's mappings are configured based on the platform
the installer is running on.  On Windows, it builds the MSI; on another platform,
it'll create and upload the other packages in the above list. (Use `s3Upload::mappings` for a dry-run.)

The version of Scala to package is derived from the most recent git tag,
or you can specify it using `-Dproject.version`.

This packager only works for Scala 2.11+ releases,
as earlier ones did not publish the `scala-dist` artifact to maven.

Due to limited resources, the native packages are quite rudimentary.
We welcome new maintainers!

## Legacy
If you're looking for the editor configurations that used to be in the tool-support directory, please see https://github.com/scala/scala-tool-support.
They were moved out because they no longer ship with the Scala distribution. (New maintainers are welcome on the scala-tool-support project!)

The specification also used to be in this repo -- it is now a part of the main repository over at [scala/scala](https://github.com/scala/scala/tree/2.11.x/spec).

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

## Contributing ##
Please read the [Scala Pull Request Policy](https://github.com/scala/scala/wiki/Pull-Request-Policy)
and sign the [CLA](http://www.lightbend.com/contribute/cla/scala).

The branching structure mimics that of [scala/scala](https://github.com/scala/scala):
branches for 2.11.x, 2.12.x, etc.

