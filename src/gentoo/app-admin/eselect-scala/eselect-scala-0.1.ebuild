# Copyright 1999-2008 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header$

DESCRIPTION="Manages Scala symlinks"
HOMEPAGE="http://www.scala-lang.org/"
SRC_URI="http://www.daimi.au.dk/~abachn/scala/distfiles/${P}.tar.bz2"

LICENSE="GPL-2"
SLOT="0"
KEYWORDS="~amd64 ~x86"
IUSE=""

RDEPEND=">=app-admin/eselect-1.0.10"

src_install() {
	insinto /usr/share/eselect/modules
	doins *.eselect || die "doins failed"
}
