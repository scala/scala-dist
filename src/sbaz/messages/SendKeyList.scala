package sbaz.messages

/** request the list of keys known to the server */
case object SendKeyList extends AbstractKeyMessage {
	def toXML = <sendkeylist/>
}
