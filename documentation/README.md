# Requirements #

* latexmk
* pdf2ps

I had to install `okumura-clsfiles` for Book.cls  and `texlive-fonts-extra`, `texlive-latex-recommended`, `texlive-latex-extra` and `texlive-fonts-recommended` for fonts on Ubuntu.


You will also need non-free fonts, ugh: http://www.tug.org/fonts/getnonfreefonts/


For users using Ubuntu 12.04 or anyone getting error: "Font ul9r8r not found":
* sudo apt-get install equivs
* equivs-control texlive-local
* wget http://www.tug.org/texlive/files/debian-control-ex.txt
* cp debian-control-ex.txt texlive-local
* equivs-build texlive-local
* sudo dpkg -i texlive-local_2011-1~1_all.deb
* sudo mkdir -p /usr/share/texmf
* sudo apt-get install auctex
* sudo vi /var/lib/texmf/web2c/updmap.cfg and add the line `Map ul9.map`
* sudo mktexlsr
* sudo update-updmap
* sudo updmap-sys


If it does not work
* sudo vi /etc/texmf/updmap.d/00updmap.cfg  and add the line `Map ul9.map`
* sudo update-updmap 
* sudo updmap-sys

Source:
* http://www.tug.org/pipermail/texhax/2012-July/019395.html
* http://www.tug.org/pipermail/texhax/2012-July/019396.html
* http://www.tug.org/pipermail/texhax/2012-July/019397.html
