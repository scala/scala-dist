The sbaz-setup script lives in a sbaz directory and can be used to
initialize a new sbaz directory based on the one the script is in.  It
is run simply as:

	 sbaz-setup  directory-to-initialize


It creates directory-to-initialize as a sbaz directory, sets its
universe to the same universe as the sbaz directory sbaz is installed
into, and installs the "base" package plus all of its dependencies.

The main purpose of the script is to ease the tension between shared
sbaz directories, where a single administrator can support several
users, and personal sbaz directories, which are fully
user-customizable.  If the shared directory includes the sbaz-setup
package, then users can use the shared sbaz directory so long as it
suits, but smoothly transition to a personal sbaz directory if and
when the shared directory becomes too constraining.

A secondary purpose of this script is to inspire similar scripts for
Linux distributions and other packaged versions of sbaz.  Demanding
users of sbaz will probably want to use their own sbaz directory, not
one installed in the system, and thus a similar script is required.
