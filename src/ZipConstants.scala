package java.util.zip ;


// XXX this is present only because I can't get my stuff compiling
// otherwise.  Indeed, ZipConstants appears to be missing
// from my rt.jar .  It looks like the compiler ought to look
// at a development .jar instead of rt.jar. Probably ZipConstants
// is included in the former but not the latter.
// (Although, maybe rt.jar should include it anyway, in case of
// compilers that don't optimize away accesses to final static int's)
trait ZipConstants { }


