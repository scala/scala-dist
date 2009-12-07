/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  James Matlik
 */
 
// $Id$
 
package sbaz.download

import java.io.File
import java.net.URL

object Download {
  type DownloadType = AnyRef {
    def link: URL
    def filename: String
  }
  trait Status
  abstract class FinalStatus extends Status {
    def isEmpty: Boolean
    def get: File
  }
  case class Ready() extends Status {
    override def toString = "Ready"
  }
  case class Running() extends Status {
    override def toString = "Running"
  }
  object Done {
    def unapply(done: Done): File = done.get
    def apply(file: File): Done = new Done(file)
  }
  class Done(file: File) extends FinalStatus {
    override def toString = "Done"
    override def isEmpty = false
    override def get = file
  }
  case class Cached(_file: File) extends Done(_file) {
    override def toString = "Cached"
    override def isEmpty = false
    override def get = super.get
  }
  case class Fail(msg:String) extends FinalStatus {
    override def toString = "Fail: " + msg
    override def isEmpty = true 
    override def get = throw new NoSuchElementException("Cannot get from Fail(" + msg + ")")
  }
  
  /* Convenience objects for general use */
  object Ready extends Ready
  object Running extends Running
  
  case class Summary(worker: DownloadWorker, status:Status, contentLength:Long, downloaded:Long, startTime: Option[Long], endTime: Option[Long])
}
