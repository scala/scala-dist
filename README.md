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


## Contributing ##
Please read the [Scala Pull Request Policy](https://github.com/scala/scala/wiki/Pull-Request-Policy) and sign the [CLA](http://typesafe.com/contribute/cla/scala).

The branching structure mimics that of [scala/scala](https://github.com/scala/scala): master is the upcoming 2.11.0 release,
and the 2.10.x branch is your target for 2.10.x features -- we'll keep merging into master as long as feasible.

## Build Setup ##



Here's a few things to build:

  * `windows:package-msi` - Builds the windows MSI.  *Requires WIX 3.6 installed*
  * `debian:package-bin`  - Builds the debian DEB file.  *Requires dpkg-deb*
  * `rpm:package-bin`     - Builds the yum RPM file.  *requires rpm*
  * `universal:package-bin` - Builds the universal zip installer
  * `universal-docs:package-bin` - Builds the universal documentation zip
