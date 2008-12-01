# Copyright 1999-2008 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header$

inherit elisp

DISTNAME="scala-2.7.2.final"

DESCRIPTION="Scala mode for Emacs"
HOMEPAGE="http://www.scala-lang.org/"
SRC_URI="http://www.scala-lang.org/downloads/distrib/files/${DISTNAME}.tgz"

LICENSE="Scala"
SLOT="0"
KEYWORDS="~amd64 ~x86"
IUSE=""

DEPEND="!app-emacs/scala-mode-svn"

RDEPEND="|| ( ~dev-lang/scala-bin-2.7.2 ~dev-lang/scala-2.7.2 )"

S="${WORKDIR}/${DISTNAME}/misc/scala-tool-support/emacs/"

SITEFILE=65${PN}-gentoo.el
DOCS="README*"

src_compile() {
	emake || die "make of .el files failed"
}
