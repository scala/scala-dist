/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import java.net.URL 
import java.io._ 
import scala.xml._ 
import sbaz.messages._ 
import sbaz.keys._

/** A SimpleUniverse is a kind of universe that downloads packages
  * only from one server.  The packages in the universe are then
  * exactly the packages supplied by the one server.
  */
class SimpleUniverse(val name:String, val location: URL)
extends Universe
{
  override def retrieveAvailable(): AvailableList = {
    val response = requestFromServer(SendPackageList()); // XXX this does not submit a Read key!
    response match {
      case LatestPackages(packs) => packs
      case _ => throw new IOException("unexpected response: " + response)
    }
  }

  override def simpleUniverses = List(this)

  /** keys remembered for this universe */
  private var keyringHolderVar: KeyRingHolder = new MemoryKeyRingHolder

  /** keys remembered for this universe */
  def keyringHolder = keyringHolderVar

  /** keys remembered for this universe */
  def keyring = keyringHolder.keyring
  
  override def keyringFilesAreIn(dir: File) = {
    val filename = new File(dir, "keyring." + name)
    val keys =
      if(filename.exists) {
        val xml = XML.load(filename.getAbsolutePath)
        KeyRing.fromXML(xml)
      } else {
        new KeyRing
      }
    keyringHolderVar = new FileBackedKeyRingHolder(keys, filename)
  }
  
  /** All keys known to this universe */
  def keys = keyring.keys
      
  /** Add a new key for future use */
  def addKey(key: Key) = {
    keyringHolder.keyring.addKey(key)
    keyringHolder.save
  }
  
  /** Forget a key and no longer use it */
  def forgetKey(key: Key) = {
    keyringHolder.keyring.removeKey(key)
    keyringHolder.save
  }
  
  /** Add to a message all known keys that match it */
  private def messageWithKeys(msg: Message): Message = {
    val matchingKeys = keys.filter(k => k.messages.matches(msg))
    msg.withKeys(matchingKeys)
  }
  
  /** Make a low-level request to the server.
    * This method blocks until the server responds.
    * It throws an exception if there is a network problem.
    */
  // XXX learn and document which exceptions are thrown
  def requestFromServer(request0: Message): Message = {
    val request = messageWithKeys(request0)
      
    val connection = location.openConnection()
    connection.setDoOutput(true)
    val out = connection.getOutputStream()
    val bytesOut = request.toXML.toString().getBytes("UTF-8")
    out.write(bytesOut)
    out.close()

    val in = connection.getInputStream()
    val respBuf = new ByteArrayOutputStream()
    def lp() {
      val dat = new Array[byte](1000)
      val n = in.read(dat)
      if (n >= 0) {
        respBuf.write(dat, 0, n)
        lp()
      }
    }
    lp()

    // XXX this should use whatever encoding the server specified,
    // not hard code it to UTF-8
    val respString = respBuf.toString("UTF-8")
    MessageUtil.fromXML(XML.load(new StringReader(respString)))
  }
  
  override def toString() = 
    "SimpleUniverse \"" + name + "\" (" + location + ")"

  def toXML = 
<simpleuniverse>
  <name>{name}</name>
  <location>{location}</location>
</simpleuniverse> ;
}


// XXX naming it SimpleUniverse causes a compiler crash
object SimpleUniverseUtil {
  def fromXML(node:Node) = {
    val name = (node \ "name").text
    val linkString = (node \ "location").text
    val link = new URL(linkString)

    new SimpleUniverse(name, link)
  }
}
