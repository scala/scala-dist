package sbaz

object SimpleUniverseSelectors {
  /** A class that designates a remote universe */
  sealed abstract class SimpleUniverseSelector

  /** Use the first simple universe within the default universe */
  case object FirstKnown
  extends SimpleUniverseSelector

  /** Pick the simple universe with the specified name */
  case class WithName(name: String)
  extends SimpleUniverseSelector

  /** Use the universe server at the named URL */
  case class WithURL(name: String)
  extends SimpleUniverseSelector
}

