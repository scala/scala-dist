/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import scala.xml.Node

// an error in parsing an XML packet
class XMLFormatError(val xml: Node) extends FormatError
