The original "registry.jar" had its last release (3.1.3) in 2003.

Since then, Windows x64 became available. If a 64-bit JVM is
installed on Winx64, then when the JNI stuff is invoked it will
try to load a 64-bit DLL, which did not exist.

I recompiled both the 32 and the 64 bit versions on
Visual Studio 2010 (Beta2), and adapted the adapted jar
so that it will first try to load a 32-bit dll, and if that
fails it will try the 64-bit dll as well.

The amount of warnings during compilation was large (it is
a very old source code), but the result seems to work in the end
(caveat emptor)

Toni
