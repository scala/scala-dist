# Copyright 1999-2005 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# Updated by Sygneca Ltd.

DISTNAME="scala-2.6.0-RC1"
DESCRIPTION="The Scala Programming Language"
HOMEPAGE="http://scala-lang.org/"
SRC_URI="http://scala-lang.org/downloads/distrib/files/$DISTNAME.tar.bz2"
LICENSE="BSD"
SLOT="0"
KEYWORDS="~x86"
IUSE=""
DEPEND=">=virtual/jdk-1.4"
RDEPEND=">=virtual/jre-1.4"
S=${WORKDIR}

src_install() {
	dodir /opt/scala
	mv $S/$DISTNAME/* $D/opt/scala/

	insinto /etc/env.d
	doins $FILESDIR/25scala
}
