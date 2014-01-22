# The Scala 2.11.x Distribution #

This project morphs the "org.scala-lang" % "scala-dist" % $version artifact
into a Scala $version distribution (zip, tar.gz, deb, rpm, and msi).

It also includes the Scala Language Specification (under documentation/),
a collection of tool support (e.g. editor configurations),
and an (unmaintained) script to generate bash completions for the common scala commands.

To build a distribution, run:

  * `universal:package-bin` - Builds the universal zip installer
  * `universal-docs:package-bin` - Builds the universal documentation zip
  * `debian:package-bin`  - Builds the debian DEB file.  *Requires dpkg-deb*
  * `rpm:package-bin`     - Builds the yum RPM file.  *requires rpmbuild*
  * `windows:package-bin` - Builds the windows MSI.  *Requires WIX 3.6 installed*

The version of Scala to package is derived from the most recent git tag,
or you can specify it using -Dproject.version.

This packager only works for Scala 2.11 releases (starting with M8),
as earlier ones did not publish the scala-dist artifact to maven.

Due to limited resources, the native packages are quite rudimental,
and tool-support isn't packaged at all.

If you'd like to maintain any of these packages, please contact scala-internals,
and we'll do our very best to support you!

## Contributing ##
Please read the [Scala Pull Request Policy](https://github.com/scala/scala/wiki/Pull-Request-Policy)
and sign the [CLA](http://typesafe.com/contribute/cla/scala).

The branching structure mimics that of [scala/scala](https://github.com/scala/scala):
master is the upcoming 2.11.0 release,
and the 2.10.x branch is your target for 2.10.x features.
