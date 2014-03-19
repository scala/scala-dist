# The Scala 2.11.x Distribution #

This project morphs the `"org.scala-lang" % "scala-dist" % $version` maven artifact
into a Scala `$version` distribution (zip, tar.gz, deb, rpm, and msi).

The following parts of this repo are *not* part of the distribution:
 - `documentation/`: the LaTeX version of the Scala Language Specification (we're moving to a [Markdown-based spec](https://github.com/adriaanm/scala-ref-markdown));
 - `tool-support/`: a collection of editor configurations etc (looking for a maintainer/packager!);
 - `bash-completion/`: generates bash completions for the common scala commands (looking for a maintainer/packager!).

To build a distribution, run:

  * `universal:package-bin` - Builds the universal zip installer
  * `universal-docs:package-bin` - Builds the universal documentation zip
  * `debian:package-bin`  - Builds the debian DEB file.  *Requires dpkg-deb*
  * `rpm:package-bin`     - Builds the yum RPM file.  *requires rpmbuild*
  * `windows:package-bin` - Builds the windows MSI.  *Requires WIX 3.6 installed*

Alternatively, the `s3-upload` task's mapping are configured based on the platform
the installer is running on: on Windows, it builds the MSI; on another platform,
it'll create and upload the other packages in the above list. (Use `s3Upload::mappings` for a dry-run.)

The version of Scala to package is derived from the most recent git tag,
or you can specify it using `-Dproject.version`.

This packager only works for Scala 2.11 releases (starting with M8),
as earlier ones did not publish the `scala-dist` artifact to maven.

Due to limited resources, the native packages are quite rudimental,
and bash-completion/tool-support aren't packaged at all.

If you'd like to maintain any of these packages, please contact scala-internals,
and we'll do our very best to support you!

## Windows VM
  - install windows 7 professional N 64-bit, ensure network access to github
  - install oracle java 6 jdk
  - install wix v3.6 (reboot!)
  - download sbt launcher 0.13.1 from xsbt.org to c:\users\jenkins\Downloads
  - install git
  - configure the jenkins master's tool locations for
     - HOME
     - jdk path
     - git (path of git.exe)
     - sbt launch jar


## Contributing ##
Please read the [Scala Pull Request Policy](https://github.com/scala/scala/wiki/Pull-Request-Policy)
and sign the [CLA](http://typesafe.com/contribute/cla/scala).

The branching structure mimics that of [scala/scala](https://github.com/scala/scala):
master is the upcoming 2.11.0 release,
and the 2.10.x branch is your target for 2.10.x features.
