/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  James Matlik
 */


package sbaz.util

import java.io.{BufferedOutputStream, File, FileOutputStream, FileWriter, 
                FileInputStream, InputStream, OutputStream}
import sbaz.Filename
import sbaz.FileUtils

object RichFile {
  //val root = new File("")
  implicit def pimpFileToRichFile(file: File) = new RichFile(file)
  implicit def pimpFilenameToFile(filename: Filename) = filename.toFile
  implicit def pimpFilenameToRichFile(filename: Filename) = new RichFile(filename.toFile)
  implicit def pimpFilenameToStringList(filename: Filename): List[String] = filename.pathComponents
  //implicit def pimpStringListToFilename(components: List[String]) = new Filename(false, true, components)
  //implicit def VarargsToStringList(parts: String*) = parts.toList
  def file(parts: List[String]): Filename = new Filename(true, true, parts)
  def relfile(parts: String*): Filename = relfile(parts.toList)
  def relfile(parts: List[String]): Filename = new Filename(true, false, parts)
  def directory(parts :List[String]): Filename = new Filename(false, true, parts)
  def reldirectory(parts: List[String]): Filename = new Filename(false, false, parts)
}

class RichFile(file: File) {
  lazy val url = file.toURI.toURL
  
  def append(f: => Any) {
    val out = new FileWriter(file, true)
    out.write(f.toString)
    out.close
  }
  
  def write(f: => Any) {
    val out = new FileWriter(file, false)
    out.write(f.toString)
    out.close
  }
  
  def cat() {
    val in = new FileInputStream(file)
    val out = System.out
    FileUtils.pipeStream(in, out)
    in.close
    out.flush  // Don't close, as that will wreck the application
  }

  def copy(to: File) = FileUtils.copyFile(file, to)
  def md5: String = FileUtils.md5(file)
  def pack200 = FileUtils.pack200(file)

  def unpack200: File = {
    val unpackFile: File = {
      val name = file.getName.substring(0, file.getName.lastIndexOf("."))
      if (".pack" != file.getName.substring(name.length))
        throw new java.io.InvalidObjectException("Can only unpack200 a \".pack\" file: " + file.getName)
      new File(file.getParent(), name + ".jar")
    }
    FileUtils.unpack200(file, unpackFile)
    unpackFile
  }
  

  def repack200 = {
    val packfile = new RichFile(file).pack200
    file.delete
    assert(!file.exists)
    new RichFile(packfile).unpack200
    assert(file.exists)
    packfile.delete
    file
  }
}
