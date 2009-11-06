/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  James Matlik
 */
 
// $Id$
 
package sbaz.download

import Download._
import java.io.File
import java.net.URL

object Downloader {
  def apply(f:File): Downloader = {
    val maxWorkers = 
      try { System.getProperty("sbaz.download.maxWorkers", "1").toInt }
      catch { case _ => 1 }
    if (maxWorkers > 1) new AsyncDownloader(f, maxWorkers)
    else new SimpleDownloader(f)
  }
}

trait Downloader {
  val dir: File
  
  def is_downloaded(name: String): Boolean =
    (new File(dir, name)).exists()

  def download(url: URL, toname: String): FinalStatus 
  def download[A <: DownloadType](dnl: A): FinalStatus 
  def download[A <: DownloadType](downloads:List[A]): Map[A, FinalStatus] 

  // Delete all downloaded files
  def flushCache {
    val files = dir.listFiles()
    if (files != null)
      for (ent <- files.toList if !ent.isDirectory()) {
        ent.delete()
      }
  }
  
  def formatBytes(size: Long): String = {
    if (size < 0) "Unknown"
    else if (size < 1000) "%dB".format(size)
    else if (size/1024 <= 1000) "%.1fK".format(size.toDouble/1024)
    else "%.2fM".format(size.toDouble/1048576)
  }

  def printStatus(backspaces: Int, text: String) = {
    val back = "\b" * backspaces
    val clearEOL = 
      if(backspaces > text.length) { 
        val count = backspaces - text.length
        " " * count + "\b" * count
      } else ""
    Console.print(back + text + clearEOL)
    text.length
  }

}
