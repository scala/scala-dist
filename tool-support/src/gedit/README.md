* Introduction

This directory contains a GTK langage specification (.lang) for Scala
programs. This language specification is used in particular by "gedit",
the lightweight text editor for the Gnome Desktop.

The latest revisions of the GTK language specifications are available from:

   http://cvs.gnome.org/viewcvs/gtksourceview/gtksourceview/language-specs/

* Installation

Copy the file "scala.lang" to the following location:

   ~/.local/share/gtksourceview-3.0/language-specs/

or alternatively to the location:

   /usr/share/gtksourceview-3.0/language-specs/

Restart your Gnome applications ("gedit", etc.).

From that point on, loading a file whose name ends in ".scala" automatically
turns Scala mode on.

* Thanks

scala.lang was contributed by Gabriel Riba (griba2010@ya.com)

