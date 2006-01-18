package sbaz.clui
import java.io.File

// Global settings for the command-line UI
class Settings {
  // the name of the directory that is being managed
  var dirname = new File(System.getProperty("scala.home", "."))

  // a ManagedDirectory opened on the same
  var dir:ManagedDirectory = null 

  // whether to actually do the requested work, or to
  // just print out what would be done
  var dryrun = false

  // The location of the miscellaneous helper files
  // needed by a ManagedDirectory.  Normally these
  // are taken from within the managed directory, but
  // developers of sbaz itself may wish to use different
  // versions.
  var miscdirname: File =
    { val str = System.getProperty("sbaz.miscdirhack")
      if(str == null)
	null
     else
       new File(str)
   }


  // XXX bogusly choose a simple universe to connect to
  def chooseSimple = {
    dir.universe.simpleUniverses.reverse(0)
  }
}
