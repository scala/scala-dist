# Copyright 1999-2008 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Headers:$

DISTNAME="scala-2.7.1.final"
DESCRIPTION="The Scala Programming Language"

HOMEPAGE="http://scala-lang.org/"
SRC_URI="http://scala-lang.org/downloads/distrib/files/$DISTNAME.tar.gz"

LICENSE="Scala"
SLOT="0"
KEYWORDS="x86 amd64"
IUSE=""

RESTRICT="nomirror"

DEPEND=">=virtual/jdk-1.4"
RDEPEND=${DEPEND}

S=${WORKDIR}
	
src_install() {
        dodir /opt/scala
	rm $S/$DISTNAME/bin/*.bat
        mv $S/$DISTNAME/* $D/opt/scala/

	insinto /etc/env.d
        doins $FILESDIR/25scala
}

