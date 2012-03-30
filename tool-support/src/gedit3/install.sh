#!/bin/sh

#
# Shell script to download and install a Scala language specification
# for the Gedit text editor.
#
# (c) Sarah Mount 26 Jan 2012.
#

# Create a directory for the language spec.
mkdir -p ~/.gnome2/gtksourceview-1.0/
mkdir -p ~/.gnome2/gtksourceview-1.0/language-specs/

cd ~/.gnome2/gtksourceview-1.0/language-specs/

# Download the spec.
wget https://raw.github.com/scala/scala-dist/master/tool-support/src/gedit/scala.lang

# Add a MIME type for Scala files.
mkdir -p ~/.local/share/mime/
mkdir -p ~/.local/share/mime/packages

cat > Scala.xml << EOF 
<mime-info xmlns='http://www.freedesktop.org/standards/shared-mime-info'>
  <mime-type type="text/x-scala">
    <comment>Scala Source</comment>
    <!-- more translated comment elements -->
    <glob pattern="*.scala"/>
  </mime-type>
</mime-info>
EOF

# Update MIME database.
cd ~/.local/share/
update-mime-database mime

# Done.
echo "Scala language specification for GEdit has been installed."

# Install the Scala plugin for on-the-fly compilation.
echo "Installing a gedit3 plugin for Scala."

# Create gedit directory structure, if it does not exist.
cd ~/.local/share
mkdir -p gedit
mkdir -p gedit/plugins
cd gedit/plugins

wget https://raw.github.com/snim2/gedit-scala-plugin/master/flyscala.plugin
wget https://raw.github.com/snim2/gedit-scala-plugin/master/flyscala.gedit-plugin
wget https://raw.github.com/snim2/gedit-scala-plugin/master/flyscala.py

echo "Plugin installed."
echo "*** IMPORTANT ***"
echo "Please start gedit and activate the Scala On The Fly plugin from the Edit->Preferences dialog"
