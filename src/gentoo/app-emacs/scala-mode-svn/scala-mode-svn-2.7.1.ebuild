# Copyright 1999-2008 Gentoo Foundation
# Distributed under the terms of the GNU General Public License v2
# $Header$

inherit elisp subversion

MY_PN=${PN//-svn/}

DESCRIPTION="Scala mode for Emacs (From Subversion)"
HOMEPAGE="http://www.scala-lang.org/"
SRC_URI=""
ESVN_REPO_URI="http://lampsvn.epfl.ch/svn-repos/scala/scala-tool-support/tags/R_2_7_1_final/src/emacs"

LICENSE="Scala"
SLOT="0"
KEYWORDS="~amd64 ~x86"
IUSE=""

DEPEND="!app-emacs/scala-mode"

RDEPEND="|| ( ~dev-lang/scala-bin-2.7.1 ~dev-lang/scala-2.7.1 )"

SITEFILE=65${MY_PN}-gentoo.el
DOCS="README*"

src_compile() {
	elisp-comp *.el || die "elisp-comp failed"
}
