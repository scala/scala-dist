/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import sbaz.clui._

object Compact extends Command {
  val name = "compact"
  val oneLineHelp = "clear the download cache to save space"
  val fullHelp: String = (
    "compact\n" +
    "\n" +
    "Compact the managed directory.  Removes all cached downloads.\n")



  def run(args: List[String], settings: Settings) = {
    import settings._
    
    if (!dryrun) {
      dir.compact
    }
  }
}
