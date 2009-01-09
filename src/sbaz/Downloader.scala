/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.net.URL
import java.io.{File, FileOutputStream}

// a class to manage downloads of files from the Internet
// into a user-specified directory

// This could be greatly improved by allowing asynhcronous downloads
// and by allowing multiple downloads to procede at the same time.
class Downloader(val dir: File) {
  def is_downloaded(name: String): Boolean =
    (new File(dir, name)).exists()

  def download(url: URL, toname: String) = {
    dir.mkdirs() // make sure the cache directory exists

    val toFile = new File(dir, toname)
    val tmpFile = new File(toFile.getAbsolutePath() + ".tmp")

    val inputStream = url.openConnection().getInputStream()

    val f = new FileOutputStream(tmpFile)
    def lp() {
      val dat = new Array[byte](1000)
      val numread = inputStream.read(dat)
      if (numread >= 0) {
        f.write(dat, 0, numread)
        lp()
      }
    }
    lp()
    f.close()

    toFile.delete()
    tmpFile.renameTo(toFile)
  }

  // Delete all downloaded files
  def flushCache {
    val files = dir.listFiles()
    if (files != null)
      for (ent <- files.toList if !ent.isDirectory()) {
        ent.delete()
      }
  }
}
