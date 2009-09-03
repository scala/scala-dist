/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

import scala.xml._
import scala.collection.mutable._
import java.io._
import sbaz.messages._ 
import sbaz.keys._

/** This class does the real processing with requests via the Servlet
  * class.  For some reason, the servlets architecture bends over backwards
  * to fight anyone who wants to have a single instance of their servlet.
  * To avoid that fight, this class can be instantiated once per directory
  * that is managed.
  */

class ServletRequestHandler(directory:File) {
  /** utility for loading an XML file */
  def loadXML[A](baseName: String, loader: Node=>A, ifmissing: =>A) = {
    val file = new File(directory, baseName)
    if (file.exists) {
      val xml = XML.load(file.getAbsolutePath)
      loader(xml)
    }
    else
      ifmissing
  }
  
  /** Rename a file.  Doesn't simply use renameTo(), because on Windows
    * it refuses to overwrite the target file. */
  private def renameFile(from: File, to: File) = {
    to.delete()
    from.renameTo(to)
  }

  
  /** utility for saving an XML file */
  private def saveXML(xml: Node,
                      filename: String) =
  {
    val tmpFile = new File(directory, filename + ".tmp")
    val str = new FileWriter(tmpFile)
    str.write(xml.toString())
    str.close()
    renameFile(tmpFile, new File(directory, filename))
  }


  val universe:SimpleUniverse =
    loadXML("universe", Universe.fromXML, null)
      match {
        case univ:SimpleUniverse => univ;
        case _ => throw new Error("this universe is not a simple universe");
      }
    

  var packages = loadXML("packages",
                         AvailableListUtil.fromXML, 
                         new AvailableList(Nil))

  def savePackages() = saveXML(packages.toXML, "packages")

  val keyring = loadXML("keyring", KeyRing.fromXML, new KeyRing)
  def saveKeyring = saveXML( keyring.toXML, "keyring")
  
  /** requests that are allowed without needing a key */
  val keylessRequests: List[MessagePattern] = {
    def fromXML(xml: Node): List[MessagePattern] =
      for {
        node <- xml.child.toList
        if node.isInstanceOf[Elem]
      }
      yield MessagePattern.fromXML(node)


    loadXML("keylessRequests", fromXML, Nil)
  }
  println("requests needing no key are: " + keylessRequests)

  def responseForGET: String = synchronized {
    val out = new StringWriter();
    out.write("This is a Scala Bazaar.  The bazaar descriptor is:\n");
    out.write(universe.toXML.toString());
    out.write("\n\n");
    out.write("The packages included are:\n");
    for(spec <- packages.sortedSpecs) {
      out.write(spec.toString());
      out.write("\n");
    }
    out.toString();
  }

  def handleRequest(req0:Message): Message = synchronized {
    val req = req0.sansKeys
    val keys = req0.authKeys.filter(k => keyring.keys.contains(k))
    if(!keylessRequests.exists(kr => kr.matches(req)) &&
       !keys.exists(k => k.messages.matches(req))) {
      NotOK("Permission denied")
    } else {      
      req match {
        case SendPackageList() =>
          LatestPackages(packages)
  
        case AddPackage(pack) =>
          println("adding new package: " + pack)
          val packsMinus = packages.available.filter(p => ! p.spec.equals(pack.spec))
          val newPacks = pack::packsMinus
          packages = new AvailableList(newPacks)
          savePackages()
          OK()
        
        case RemovePackage(spec) =>
          println("removing package: " + spec);
  	
          val packsMinus = packages.available.filter(p => ! p.spec.equals(spec))
          packages = new AvailableList(packsMinus)
          savePackages()
          OK()
  
        case KeyCreate(messages, description) =>
          val data = KeyUtil.genKeyData
          val key = new Key(messages, description, data)
          keyring.addKey(key)
          saveKeyring
          KeyCreated(key)
        
        case KeyRevoke(key) =>
          keyring.removeKey(key)
          saveKeyring
          OK()
        
        case SendKeyList =>
          KeyList(keyring.keys)
  
        case _ =>
          NotOK("unhandled message type.  full message: " + req);
      }
    }
  }

}


object ServletRequestHandler {
  // a map from canonical filenames to the handler for that directory
  private val handlers = new HashMap[String,ServletRequestHandler]();

  // Find the handler for a specified directory.  Create a new one if
  // necessary.
  def handlerFor(directory:File): ServletRequestHandler = synchronized {
    val fncanon = directory.getCanonicalPath();
    if(!handlers.contains(fncanon)) {
      val handler = new ServletRequestHandler(directory);
      handlers.update(fncanon, handler);
    }
    handlers(fncanon)
  }

  // convenience method for accessing the above
  def handlerFor(dirname:String): ServletRequestHandler = {
    handlerFor(new File(dirname)) 
  }
}
