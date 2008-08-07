# Copyright 1999-2008 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Headers:$

DISTNAME="scala-2.7.1.final"
DESCRIPTION="The Scala Programming Language"

HOMEPAGE="http://scala-lang.org/"
SRC_URI="http://scala-lang.org/downloads/distrib/files/$DISTNAME.tar.gz"

LICENSE="Scala"
SLOT="0"
KEYWORDS="~amd64 ~x86"
IUSE="sources sbaz"

RESTRICT="nomirror"

DEPEND=">=virtual/jdk-1.4"
RDEPEND=${DEPEND}

S=${WORKDIR}

SCALA_BIN="fsc  scala  scalac  scalap  scaladoc"
SCALA_LIB="scala-compiler.jar  scala-dbc.jar  scala-decoder.jar  scala-library.jar"
SCALA_MAN="fsc.1  scala.1  scalac.1  scaladoc.1  scalap.1"

SBAZ_BIN="sbaz  sbaz-setup"
SBAZ_LIB="sbaz-tests.jar  sbaz.jar"
SBAZ_MAN="sbaz.1"
	
src_install() {
        dodir /opt/scala
	
	dodir /opt/scala/bin
	for SCBIN in ${SCALA_BIN} ; do
	    mv "${S}/${DISTNAME}/bin/${SCBIN}" "${D}/opt/scala/bin/";
	done
	
	dodir /opt/scala/lib
	for SCLIB in ${SCALA_LIB} ; do
	    mv "${S}/${DISTNAME}/lib/${SCLIB}" "${D}/opt/scala/lib/";
	done
	
	dodir /opt/scala/man/man1
	for SCMAN in ${SCALA_MAN} ; do
	    mv "${S}/${DISTNAME}/man/man1/${SCMAN}" "${D}/opt/scala/man/man1/";
	done
	
	if use sources ; then
	    dodir /opt/scala/src
	    mv "${S}/${DISTNAME}/src/*" "${D}/opt/scala/src/"
	fi
	
        if use sbaz ; then

	    for SZBIN in ${SBAZ_BIN} ; do
		mv "${S}/${DISTNAME}/bin/${SZBIN}" "${D}/opt/scala/bin/";
	    done
	    
	    for SZLIB in ${SBAZ_LIB} ; do
		mv "${S}/${DISTNAME}/lib/${SZLIB}" "${D}/opt/scala/lib/";
	    done
	    
	    for SZMAN in ${SBAZ_MAN} ; do
		mv "${S}/${DISTNAME}/man/man1/${SZMAN}" "${D}/opt/scala/man/man1/";
	    done
	    
	    mv "${S}/${DISTNAME}/doc" "${D}/opt/scala/"
	    mv "${S}/${DISTNAME}/meta" "${D}/opt/scala/"

	    dodir /opt/scala/misc
	    mv "${S}/${DISTNAME}/misc/sbaz" "${D}/opt/scala/misc/"
	    mv "${S}/${DISTNAME}/misc/sbaz-testall" "${D}/opt/scala/misc/"
	fi

	insinto /etc/env.d
        doins $FILESDIR/25scala
}

