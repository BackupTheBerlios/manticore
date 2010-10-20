# Copyright 2010 anticore-projects.com
# Distributed under the terms of the GNU General Public License v2

inherit java-pkg-2 eutils fdo-mime

# TODO use versionator
MY_PV="${PV//_/}"

DESCRIPTION="Open source trading software for short and mid-term investing in shares, indices, comodities and currencies."
HOMEPAGE="http://www.manticore-projects.com/investment.htm"
SRC_URI="http://www.manticore-projects.com/download/${PN}-${MY_PV}.zip"
LICENSE="GPL-2"
KEYWORDS="~amd64 ~ppc ~ppc64 ~x86 ~x86-fbsd"
IUSE="firebird mysql postgres"
SLOT="0"

RDEPEND=">=virtual/jre-1.6"
DEPEND=">=virtual/jdk-1.6"

PROGRAM_HOME="/opt/${PN}"

src_install () {
	dodir ${PROGRAM_HOME}
	cp -R ./* "${D}/${PROGRAM_HOME}"

	java-pkg_regjar ${D}/${PROGRAM_HOME}/dist/${PN}.jar
	java-pkg_regjar ${D}/${PROGRAM_HOME}/dist/lib/*.jar
	
	java-pkg_dolauncher ${PN} --jar ${PN}.jar --pwd ${PROGRAM_HOME}/dist
	
	make_desktop_entry ${PN} ${PN} ${PROGRAM_HOME}/${PN}.png "Office"
}

pkg_postinst() {
	fdo-mime_desktop_database_update
}

pkg_postrm() {
	fdo-mime_desktop_database_update
}
