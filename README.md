# The Scala Distribution #

This project contains all the bits of Scala that don't belong in trunk.  This includes but is not limited to:

  * Additional Documentation
    * The Scala Specification
    * Scala by example
  * Example code
  * Native packages
    * msi
    * deb
    * rpm
  * Support for third-party tools
    * vi
    * gedit
    * more!


## Build Setup ##

This build requires a scala distrbution build from the [scala/scala](https://github.com/scala/scala) project.   Here's how to obtain one and tell this build about it:

  * scala/scala must be in the user home directory
  * Please run `ant dist-opt` in in the `scala/scala` project.
  * zip the `<scala/scala>/dist/latest` directory into a file called `scala-dist.zip`
  * Copy `scala-dist.zip` into `<scala/scala-dist>/target/tmp/scala-dist.zip`
  * Start [sbt](https://github.com/harrah/xsbt) in the `<scala/scala-dist>` directory.
  * Run one of the build commands.


Here's a few things to build:

  * `examples/compile` - Compiles the examples
  * `scala-installer/windows:package-msi` - Builds the windows MSI.  *Requires WIX 3.6 installed*
  * `scala-installer/debian:package-bin`  - Builds the debian DEB file.  *Requires dpkg-deb*
  * `scala-installer/rpm:package-bin`     - Builds the yum RPM file.  *requires rpm*
  * `scala-installer/universal:package-bin` - Builds the universal zip installer
  * `scala-installer/universal-docs:package-bin` - Builds the universal documentation zip
