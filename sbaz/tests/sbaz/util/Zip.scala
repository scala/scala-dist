/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  James Matlik
 */


package sbaz.util

import java.io.{File, FileInputStream, FileOutputStream, FileNotFoundException}
import java.util.zip.{ZipFile, ZipEntry, ZipInputStream, ZipOutputStream}
import sbaz.Filename

object Zip {
  def create(outfile: File, parent:File, infiles:List[Filename]) {
    infiles.foreach { filename => 
      val file = filename.relativeTo(parent)
      if (!file.exists) throw new FileNotFoundException(file.getAbsolutePath)
    }
  
    val out = new ZipOutputStream(new FileOutputStream(outfile))

    infiles.foreach { filename =>
      // Add ZIP entry to output stream.
      out.putNextEntry(new ZipEntry(filename.toString))

      // Transfer bytes from the file to the ZIP file
      val file = filename.relativeTo(parent)
      val in = new FileInputStream(file)
      val buf = new Array[Byte](1024)
      def lp() {
        val numread = in.read(buf)
        if (numread >= 0) {
          out.write(buf, 0, numread)
          lp() 
        }
      }
      lp()

      // Complete the entry
      out.closeEntry()
      in.close()
    }

    // Complete the ZIP file
    out.close();
  }
}
