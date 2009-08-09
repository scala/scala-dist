/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  James Matlik
 */
 
// $Id$
 
package sbaz.download

import java.io.File
import java.net.URL
import scala.actors.Actor
import Actor._
import Download._

/**
 * A multi-threaded class to manage downloads of files from the
 * Internet into a user-specified directory.
 */
class AsyncDownloader(val dir: File, val maxConcurrentDownloads: Int) extends Downloader {  
  val consoleLineLength = 80

  case class Dnl(link: URL, filename: String)
  case class Results[A <: DownloadType](results:List[(A, FinalStatus)])
  case class StartDownloads[A <: DownloadType](downloads: List[A])
 
  val monitor = new Actor {  
    def act = receive { 
      case StartDownloads(downloads) => sender ! Results(downloadLoop(downloads, Nil, Nil)); exit()
    } 
  }

  /** Method intended for DownloadWorkers to provide updates to Downloader */
  private[download] def !(s: Summary) = monitor ! s

  def download(link: URL, toname: String): FinalStatus = {
    download(Dnl(link, toname))
  }

  /** Download a single target */
  def download[A <: DownloadType](dnl:A): FinalStatus = {
    dir.mkdirs() // make sure the cache directory exists
    monitor.start
    val result = monitor !? StartDownloads( dnl :: Nil) match {
      case Results(results) => results.first._2
      case _ => throw new RuntimeException("Unexpected return for StartDownloads")
    }
    output("")  // Clear the current line
    result
  }
  
  /** Download multiple targets at once asynchronously */
  def download[A <: DownloadType](downloads:List[A]): Map[A, FinalStatus] = {
    dir.mkdirs()  // make sure the cache directory exists
    monitor.start
    val result = monitor !? StartDownloads(downloads) match {
      case Results(results)=> results.asInstanceOf[List[(A, FinalStatus)]]
      case _ => throw new RuntimeException("Unexpected return for StartDownloads")
    }
    //result match {
    //  case Done() => output("Downloads completed successfully\n")
    //  case Fail(msg) => output("Download did not complete: " + msg + "\n")
    //}
    Map.empty ++ result
  }
 
  private def downloadLoop[A <: DownloadType](downloads:List[A], unitsOfWork:List[UnitOfWork[A]], results:List[(A, FinalStatus)]) : List[(A, FinalStatus)] = {
    val (d, u, r) = spawnDownloads(downloads, unitsOfWork, results)
    if (d.isEmpty && u.isEmpty) {
      output("")
      return r
    }
    output( buildProgressString(u))
    val (nextDownloads, nextUnitsOfWork, nextResults) = receive { 
      case Summary(worker, status, contentLength, downloaded, startTime, endTime) if status.isInstanceOf[FinalStatus] => {
        // If FinalSatus, move from workers List to results List
        val finished = u.find( unit => unit.worker == worker).get
        (d, u - finished, (finished.download, status.asInstanceOf[FinalStatus]) :: r)
      }
      case s @ Summary(sworker, status, contentLength, downloaded, startTime, endTime) => {
        val updatedUnits = u.map(unit => unit match {
          case UnitOfWork(download, uworker, step, lastSummary, updates) if uworker == sworker =>  UnitOfWork(download, uworker, step, Some(s), updates +1)
          case uow @ UnitOfWork(_, _, _, _, _)  =>  uow
        })
        (d, updatedUnits, r)
      }
    }
    downloadLoop(nextDownloads, nextUnitsOfWork, nextResults)
  }
  
  /**
   * Consume head nodes of the packages list to create DownloadWorkers for concurrent download
   */
  private def spawnDownloads[A <: DownloadType](downloads:List[A], workers:List[UnitOfWork[A]], finished:List[(A, FinalStatus)]): (List[A], List[UnitOfWork[A]], List[(A, FinalStatus)]) = {
    if (workers.length < maxConcurrentDownloads && downloads.length > 0) {
      val finishedCount = finished.length
      val step = if (downloads.length + workers.length + finishedCount > 1) finishedCount + workers.length + 1 else -1
      val dnl = downloads.head
      val file = new File(dir, dnl.filename)
      // Returned cached if it already exists
      if (file.exists) { 
        return spawnDownloads(downloads.tail, workers, (dnl, Cached(file)) :: finished)
      }
      val worker = new DownloadWorker(this, dnl.link, file)
      val unit = new UnitOfWork(dnl, worker, step, None, 0)
      output(buildWorkStartedString(unit)+"\n")
      return spawnDownloads(downloads.tail, unit :: workers, finished)
    }
    else return (downloads, workers, finished)
  }

  private val _spinner = Array("|", "/", "-", "\\")
  def spinner(x: Long) = _spinner(x/8%4 toInt)

  def buildProgressString[A <: DownloadType](workers:List[UnitOfWork[A]]): String = {
    val progress = workers.map(unit => {
      val prefix = if (unit.step > 0) "[%1$s]%2$d:" else "[%s]"
      if (unit.lastSummary.isDefined) {
        val summary = unit.lastSummary.get
        if (summary.contentLength > 0) {
          val contentLength = formatBytes(summary.contentLength)
          (prefix + "%3$3d%% of %4$s").format(spinner(unit.updates), unit.step, 100*summary.downloaded/summary.contentLength, contentLength)
        }
        else {
          val downloaded = formatBytes(summary.downloaded)
          (prefix + "%3$s of --B").format(spinner(unit.updates), unit.step, downloaded)
        }
      }
      else { (prefix + "---%% of --B").format(spinner(unit.updates), unit.step) }
    })
    progress.reduceLeft((a, b) => b + "  " + a)
  }

  def buildWorkStartedString[A <: DownloadType](unit: UnitOfWork[A]): String = {
    if (unit.step > 0) unit.step.toString + ": " + unit.download.link.toString else unit.download.link.toString
  }

  //TODO: I got lazy... Make this functional and side-effect free
  //TODO: Improve line blanking when newline exists early in output string
  var lastOutputLength = 0
  def output(s: String) {
    val newline = "\n"
    val backspaces = "\b" * lastOutputLength
    val clearEOL = if (lastOutputLength > s.length) {
      val numChars = lastOutputLength - s.length
      " " * numChars + "\b" * numChars
    } else ""
    Console.print(backspaces + s + clearEOL)
    Console.flush
    lastOutputLength = s.length - s.lastIndexOf(newline) -1
  }
}

case class UnitOfWork[A <: DownloadType](download: A, worker: DownloadWorker, step: Int, lastSummary: Option[Summary], updates: Long)

import java.io.{File, FileOutputStream}
import Download._
import Actor._
class DownloadWorker(downloader:AsyncDownloader, url:URL, toFile:File) {
  private[DownloadWorker] case class Update(status: Status, bytes: Int)
  //private[DownloadWorker] case class Poll()
  
  //def getSummary() = (nonBlockingActor !? Poll()).asInstanceOf[Summary]
        
  private[DownloadWorker] val nonBlockingActor = actor {
    def nonBlockingLoop(summary:Summary) {
      val nextSummary: Option[Summary] = receive {
        case s @ Summary(_, _, _, _, _, _) => Some(s)
        case Update(status, bytes) if status.isInstanceOf[FinalStatus] => {
          val finalSummary = Summary(DownloadWorker.this, status, summary.contentLength, bytes, 
  	                                summary.startTime, Some(System.currentTimeMillis))
          downloader ! finalSummary
          None // Exit the loop
        }
        case Update(status, bytes) => {
          val newSummary = Summary(DownloadWorker.this, status, summary.contentLength, bytes, summary.startTime, None)
          downloader ! newSummary
          Some(newSummary)
        }
        //case Poll() => {
        //  sender ! summary
        //  Some(summary)
        //}
      }
      // Recursive call to loop must be outside receive to prevent StackOverflowError
      if (nextSummary.isDefined) nonBlockingLoop(nextSummary.get)
    }
    nonBlockingLoop(Summary(DownloadWorker.this, Ready, -1, 0, None, None))
  }    
    
    
  /** 
   * This actor performs the actual download and the blocking IO that goes
   * with it. It produces update messages to the non-blocking DownloadWorker
   * (a.k.a. monitor) actor. It does not consume any messages.
   */
  private[DownloadWorker] val blockingActor = new Thread[Actor] {
    // The Thread needs to be explicitly created here, otherwise the Thread
    // pooling and limits for the Actor API will apply, potentially causing
    // the asynchronous downloads becomming synchronous due to resource starvation.
    override def run {
      try {
        val startTime = System.currentTimeMillis
        toFile.delete()
        val tmpFile = new File(toFile.getAbsolutePath() + ".tmp")
        val f = new FileOutputStream(tmpFile)
        val connection = url.openConnection()
        val inputStream = connection.getInputStream()
        val contentLength = connection.getContentLength()
        nonBlockingActor ! Summary(DownloadWorker.this, Running, contentLength, 0, Some(startTime), None)

        def downloadLoop(downloadedBytes:Int) {
          val dat = new Array[byte](1024)
          val numread = inputStream.read(dat)
          if (numread >= 0) {
            f.write(dat, 0, numread)
            val bytes = downloadedBytes + numread
            nonBlockingActor ! Update(Running, bytes)
            downloadLoop(bytes)
          } else {
            inputStream.close()
            f.close()
            tmpFile.renameTo(toFile)
            nonBlockingActor ! Update(Done(toFile), downloadedBytes)
            // Exit loop
          }
        }
        downloadLoop(0)
      } catch {
        case e: Exception => nonBlockingActor ! Update(Fail(e.getLocalizedMessage), 0)
      }
    }
  }
  blockingActor.start()
}
