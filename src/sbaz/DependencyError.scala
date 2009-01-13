/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

// An error in dependency management.  These are most frequently
// thrown by the routines of AvailableList.
class DependencyError(explanation: String)
extends Error(explanation) {
  def this() = this("dependency error")
}

