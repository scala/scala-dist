/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import scala.xml.Node

// an error in parsing an XML packet
class XMLFormatError(val xml: Node) extends FormatError
