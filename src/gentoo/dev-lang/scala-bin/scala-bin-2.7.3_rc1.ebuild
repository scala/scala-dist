# Copyright 1999-2008 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Headers:$

DISTNAME="scala-2.7.3.RC1"
DESCRIPTION="The Scala Programming Language"

HOMEPAGE="http://scala-lang.org/"
SRC_URI="http://www.scala-lang.org/downloads/distrib/files/$DISTNAME.tgz"


LICENSE="Scala"
SLOT="0"
KEYWORDS="~amd64 ~x86"
IUSE="sources sbaz"

RESTRICT="nomirror"

DEPEND=">=virtual/jdk-1.4
	>=app-admin/eselect-scala-0.2"
RDEPEND=${DEPEND}

S=${WORKDIR}

SCALA_BIN="fsc  scala  scalac  scaladoc"
SCALA_LIB="scala-compiler.jar  scala-dbc.jar  scala-library.jar"
SCALA_MAN="fsc.1  scala.1  scalac.1  scaladoc.1"

SBAZ_BIN="sbaz  sbaz-setup"
SBAZ_LIB="sbaz-tests.jar  sbaz.jar"
SBAZ_MAN="sbaz.1"

SCALA_ROOT="/usr/share/scala"

src_install() {
	local lroot="${SCALA_ROOT}/binary"
	local f

	dodir "${lroot}"

	dodir "${lroot}/bin"
	for f in ${SCALA_BIN} ; do
		mv "${S}/${DISTNAME}/bin/${f}" "${D}/${lroot}/bin/";
	done

	dodir "${lroot}/lib"
	for f in ${SCALA_LIB} ; do
		mv "${S}/${DISTNAME}/lib/${f}" "${D}/${lroot}/lib/";
	done

	dodir "${lroot}/man/man1"
	for f in ${SCALA_MAN} ; do
		mv "${S}/${DISTNAME}/man/man1/${f}" "${D}/${lroot}/man/man1/";
	done

	dodir "${SCALA_ROOT}/versions"
	dosym "${lroot}/bin/scala" "${SCALA_ROOT}/versions/.${P}"
	touch "${lroot}/.${P}"

	if use sources ; then
		dodir "${lroot}/src"
		mv "${S}/${DISTNAME}/src/*" "${D}/${lroot}/src/"
	fi

	if use sbaz ; then

		for f in ${SBAZ_BIN} ; do
			mv "${S}/${DISTNAME}/bin/${f}" "${D}/${lroot}/bin/";
		done

		for f in ${SBAZ_LIB} ; do
			mv "${S}/${DISTNAME}/lib/${f}" "${D}/${lroot}/lib/";
		done

		for f in ${SBAZ_MAN} ; do
			mv "${S}/${DISTNAME}/man/man1/${f}" "${D}/${lroot}/man/man1/";
		done

		mv "${S}/${DISTNAME}/doc" "${D}/${lroot}/"
		mv "${S}/${DISTNAME}/meta" "${D}/${lroot}/"

		dodir "${lroot}/misc"
		mv "${S}/${DISTNAME}/misc/sbaz" "${D}/${lroot}/misc/"
		mv "${S}/${DISTNAME}/misc/sbaz-testall" "${D}/${lroot}/misc/"
	fi
}

pkg_postinst() {
	eselect scala update
}
