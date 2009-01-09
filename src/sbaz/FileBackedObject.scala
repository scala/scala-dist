/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.io.{File, FileWriter}
import scala.xml.Node


/** An object that is backed by an XML file */
trait FileBackedObject {
  /** the file to save the object to */
  val backingFile: File
  
  /** convert the object to XML */
  def toXML: Node
  
  /** Rename a file.  Don't use File.renameTo(), because
    * on Windows it refuses to overwrite the target file.
    */
  private def renameFile(from: File, to: File) {
    to.delete()
    from.renameTo(to)
  }
  
  /** Save the object to its backing file */
  def save {
    val tmpFile = new File(backingFile.getAbsolutePath + ".tmp")
    val str = new FileWriter(tmpFile)
    str.write(toXML.toString())
    str.close()
    renameFile(tmpFile, backingFile)
  }
}
