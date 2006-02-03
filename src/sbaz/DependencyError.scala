package sbaz;

// An error in dependency management.  These are most frequently
// thrown by the routines of AvailableList.
class DependencyError(explanation: String)
extends Error(explanation) {
  def this() = this("dependency error")
}

