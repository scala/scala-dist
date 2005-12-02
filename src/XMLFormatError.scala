package sbaz;
import scala.xml.Node;

// an error in parsing an XML packet
class XMLFormatError(val xml:Node)
extends FormatError ;
