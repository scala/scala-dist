# Scala 2.13 Distribution

This project morphs the `"org.scala-lang" % "scala-dist" % $version` maven artifact
into a Scala `$version` distribution (zip, tar.gz, deb, rpm, and msi).

To build a distribution, run:

  * `Universal/packageBin` - Builds the universal zip installer
  * `UniversalDocs/packageBin` - Builds the universal documentation zip
  * `Debian/packageBin`  - Builds the Debian DEB file.  *requires dpkg-deb*
  * `Rpm/packageBin`     - Builds the yum RPM file.  *requires rpmbuild*
  * `Windows/packageBin` - Builds the Windows MSI.  *requires WiX 3.6*

Alternatively, `ghUpload` builds the packages for the platform it runs on and uploads them
to the GitHub release: the MSI on Windows, the others elsewhere. (Use `show ghUpload/mappings`
to build them without uploading.)

The version of Scala to package is derived from the most recent git tag,
or you can specify it using `-Dproject.version`.

## Releasing

The "Distribution" GitHub Actions workflow builds the packages on Linux and Windows. For a
release, run it from the Actions tab with the Scala version, the scala/scala commit SHA and a mode:

  * `release` uploads the packages to the GitHub release, which must exist already,
    then runs [scala-dist-smoketest](https://github.com/scala/scala-dist-smoketest) on them.
  * `archives` copies the packages to chara for scala-lang.org.
  * `update-api` points the `2.13.x` API docs link to this version.

It needs two secrets: `SCALA_REPO_TOKEN`, which can upload release assets and post
commit statuses on scala/scala, and `CHARA_SSH_KEY`, the private key for chara.

## Contributing

Please sign the [CLA](https://contribute.akka.io/contribute/cla/scala).

The branching structure mimics that of [scala/scala](https://github.com/scala/scala);
there are branches for 2.12.x and 2.13.x.
