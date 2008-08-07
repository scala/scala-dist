# Copyright 1999-2007 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header: $

inherit eutils

MY_PV=${PV//./_}
MY_P=smartsvn-generic-${MY_PV}

DESCRIPTION="SmartSVN"
HOMEPAGE="http://www.syntevo.com/smartsvn/"
SRC_URI="${MY_P}.tar.gz"
SLOT="0"
LICENSE="smartsvn"
KEYWORDS="~x86 ~amd64"

IUSE=""
RESTRICT="fetch nomirror"

RDEPEND=">=virtual/jre-1.4.1"

S="${WORKDIR}/smartsvn-${MY_PV}"

src_unpack() {
	unpack ${A}
}

src_compile() {
	mv ${WORKDIR}/SmartSVN\ 4.0.1 ${S}
}

src_install() {
	dodir /opt/smartsvn
	cp -dPR * ${D}/opt/smartsvn/
	dodir /usr/bin
	dosym /opt/smartsvn/bin/smartsvn.sh /usr/bin/

	for X in 32 48 64 128
	do
		insinto /usr/share/icons/hicolor/${X}x${X}/apps
		newins ${S}/bin/smartsvn-${X}x${X}.png ${PN}.png
	done

	make_desktop_entry ${PN}.sh "SmartSVN" ${PN}.png "Development;RevisionControl"
}

pkg_nofetch() {
	einfo "Please download ${MY_P}.tar.gz from:"
	einfo "http://www.syntevo.com/smartsvn/download.html?file=smartsvn/smartsvn-generic-4_0_1.tar.gz"
	einfo "and move/copy to ${DISTDIR}"
}


