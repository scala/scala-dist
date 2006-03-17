package sbaz.messages;

/** An abstract class designating that a message
  * class involves an operation on the keys
  * of a bazaar server.  This is used for
  * security policies; see class sbaz.keys.msgpatt.EditKeys.
  */
abstract class AbstractKeyMessage extends Message
