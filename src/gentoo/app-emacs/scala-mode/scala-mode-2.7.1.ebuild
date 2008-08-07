# Copyright 1999-2008 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header$

inherit elisp

DESCRIPTION="Scala mode for Emacs"
HOMEPAGE="http://www.scala-lang.org/"
SRC_URI="http://www.scala-lang.org/downloads/distrib/files/scala-${PV}.final.tar.gz"

LICENSE="Scala"
SLOT="0"
KEYWORDS="~amd64 ~x86"
IUSE=""

DEPEND="!app-emacs/scala-mode-svn"

RDEPEND="|| ( ~dev-lang/scala-bin-2.7.1 ~dev-lang/scala-2.7.1 )"

S="${WORKDIR}/scala-${PV}.final/misc/scala-tool-support/emacs/"

SITEFILE=65${PN}-gentoo.el
DOCS="README*"

src_compile() {
	elisp-comp *.el || die "elisp-comp failed"
}
