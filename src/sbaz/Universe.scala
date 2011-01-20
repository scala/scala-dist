/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import java.io.{File,FileReader,FileInputStream}
import java.net.URL
import java.util.regex._

import scala.collection.mutable.ListBuffer
import scala.xml._


/** A universe is a visible set of available packages that
  * can change over time.
  */
abstract class Universe {
  def toXML: Node

  def retrieveAvailable(): AvailableList

  def simpleUniverses: List[SimpleUniverse]

  /** Inform this universe that it can save its keyring
    * files in the specified directory.  This is only
    * meaningful for client programs.
    */
  def keyringFilesAreIn(dir: File) {}
}


object Universe {
  /** Load a universe described in XML */
  def fromXML(node: Node): Universe = {
    node match {
      case node: Elem =>
        val name = node.label
        name match {
          case "overrideuniverse" =>
            OverrideUniverse.fromXML(node)

          case "simpleuniverse" =>
            SimpleUniverseUtil.fromXML(node)

          case "emptyuniverse" =>
            new EmptyUniverse()

          case _ =>
            throw new XMLFormatError(node)
        }

      case _ =>
        throw new XMLFormatError(node)
    }
  }

  /** Read a file's contents into a string, using
   *  the platform's default character encoding. */
  private def readEntireFile(file: File): String = FileUtils.readFile(file)

  /** A compiled regex for matching a name and a URL */
  private val nameAndUrlPattern =
    Pattern.compile("[ \t]*([^ \t]+)[ \t]+([^ \t]+)[ \t]*")
 
  /** Load a universe from a string that is in the "custom" format, i.e.
   *  with one universe per line. */
  private def fromCustomFormat(str: String): Universe = {
    val simpUnivs = new ListBuffer[SimpleUniverse]
    for (line <- str.lines) {
      val matcher = nameAndUrlPattern.matcher(line)
      if (matcher.matches) {
        val name = matcher.group(1)
        val url = new URL(matcher.group(2))
        simpUnivs += new SimpleUniverse(name, url)
      } else if (line.trim != "") {
        throw new FormatError("bad line in universe descriptor: " + line)
      }
    }

    simpUnivs.toList match {
      case Nil => new EmptyUniverse()
      case alone :: Nil => alone
      case univs => new OverrideUniverse(univs)
    }
  }

  /** Load a universe from a string, using either the XML format,
   *  or the short, line-by-line format.  */
  def fromString(str: String): Universe =
    if (str.trim.startsWith("<"))
      fromXML(XML.loadString(str))
    else
      fromCustomFormat(str)

  /** Load a universe from a file, using either the XML format
   *  or the short, line-by-line format. If the file does not
   *  exist, then return the empty universe. */
  def fromFile(file: File): Universe = {
    if (file.exists) {
      val str = new FileInputStream(file)
      val firstc: Int = str.read()
      str.close()
      if (firstc == '<'.toInt) {
        // load using XML.load to take care of character
        // encoding
        fromXML(XML.loadFile(file))
      } else {
        fromCustomFormat(readEntireFile(file))
      }
    } else
      new EmptyUniverse()
  }
}
