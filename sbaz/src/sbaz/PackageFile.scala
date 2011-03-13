/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import java.io.File
import java.util.zip.{ZipFile, ZipEntry}
import scala.xml.XML
import scala.collection.mutable.ListBuffer

/** A file containing a package.  Throws a FormatError when it is instantiated if
  * the file is not properly formated.
  *
  * The file is in ZIP format.  The meta/description file within the ZIP
  * file includes the meta-information about the package; see class Package.
  * All other entries in meta/ should be ignored; they are reserved for
  * future versions of the package format.  All entries other than ones in
  * meta/ are files that should be installed if the package is installed.
  */
class PackageFile(val file: File)
{
  def this(filename: String) = this(new File(filename))
  
  /** Open a ZipFile on this package's file.  The ZipFile should
    * be closed when the caller is finished.
    */
  def openZip = new ZipFile(file)
  
  /** The meta-information about this package */
  val pack = {
    val zip = openZip
    val ent = zip.getEntry("meta/description")
    if (ent == null)
      throw new FormatError("malformed package file: meta/description is missing")  

    val inBytes = zip.getInputStream(ent)
    val packXML = XML.load(inBytes)
    inBytes.close
    zip.close
        
    PackageUtil.fromXML(packXML)
  }
  
  /** The list of filenames included in this package */
  def fileNames = {
    val zip = openZip
    val names = new ListBuffer[String]

    val enum = zip.entries
    while (enum.hasMoreElements) {
      val ent = enum.nextElement.asInstanceOf[ZipEntry]
      val name = ent.getName
      if (!name.startsWith("meta/"))
        names += name
    }
    
    names.toList
  }
}
