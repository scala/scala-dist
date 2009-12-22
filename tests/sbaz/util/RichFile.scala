/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  James Matlik
 */

// $Id$

package sbaz.util

import java.io.{BufferedOutputStream, File, FileOutputStream, FileWriter, 
                FileInputStream, InputStream, OutputStream}
import sbaz.Filename
//import sbaz.Filename._

object RichFile {
  val root = new File("")
  implicit def pimpFileToRichFile(file: File) = new RichFile(file)
  implicit def pimpFilenameToFile(filename: Filename) = filename.relativeTo(root)
  implicit def pimpFilenameToRichFile(filename: Filename) = new RichFile(filename.relativeTo(root))
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
  def append(text: String) {
    val out = new FileWriter(file, true)
    out.write(text)
    out.close
  }
  
  def write(text: String) {
    val out = new FileWriter(file, false)
    out.write(text)
    out.close
  }
  
  def cat() {
    val in = new FileInputStream(file)
    val out = System.out
    pipe(in, out)
    in.close
    out.flush  // Don't close, as that will wreck the application
  }

  def copy(dest: File) {
    val in = new FileInputStream(file)
    val out = new FileOutputStream(dest)
    pipe(in, out)
    in.close
    out.close
  }

  /** Pipe Bytes from an InputStream to an OutputStream */
  private def pipe(in: InputStream, out: OutputStream) {
    val buf = new Array[Byte](1024)
    def lp() {
      val numread = in.read(buf)
      if (numread >= 0) {
        out.write(buf, 0, numread)
        lp()
      }
    }
    lp()
  }


  def md5 = {
    import java.math.BigInteger
    import java.security.MessageDigest
    val digester = MessageDigest.getInstance("MD5");
    val in = new FileInputStream(file)
    val buf = new Array[Byte](1024)
    def lp() {
      val numread = in.read(buf)
      if (numread >= 0) {
        digester.update(buf, 0, numread)
        lp() 
      }
    }
    lp()
    in.close
    val md5 = new BigInteger(1,digester.digest()).toString(16);
    "0" * (32-md5.length) + md5
  }
  
  def pack200 = {
    import java.util.jar.{JarFile, Pack200}
    val packer = Pack200.newPacker
    val packFile: File = {
      val name = file.getName.substring(0, file.getName.lastIndexOf("."))
      if (".jar" != file.getName.substring(name.length))
        throw new java.io.InvalidObjectException("Can only pack200 a \".jar\" file: " + file.getName)
      new File(file.getParent(), name + ".pack")
    }
    val os = new BufferedOutputStream(new FileOutputStream(packFile))
    packer.pack(new JarFile(file), os)
    os.close
    packFile
  }

  def unpack200 = {
    import java.util.jar.{JarOutputStream, Pack200}
    val unpacker = Pack200.newUnpacker
    val unpackFile: File = {
      val name = file.getName.substring(0, file.getName.lastIndexOf("."))
      if (".pack" != file.getName.substring(name.length))
        throw new java.io.InvalidObjectException("Can only unpack200 a \".pack\" file: " + file.getName)
      new File(file.getParent(), name + ".jar")
    }
    val os = new JarOutputStream(new FileOutputStream(unpackFile));
    unpacker.unpack(file, os)
    os.close
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
