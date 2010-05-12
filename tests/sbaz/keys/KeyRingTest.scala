/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.keys

import junit.framework._
import junit.framework.Assert._

import scala.collection.immutable.ListSet

import sbaz.keys.{msgpatt => MP}

class KeyRingTest extends TestCase {
  val ring = new KeyRing
  ring.addKey(Key(MP.EditKeys, "editkeys", "1234"))
  ring.addKey(Key(MP.Edit(".*"), "editall", "1235"))
  ring.addKey(Key(MP.Edit("finger"), "edit-finger", "1236"))
  ring.addKey(Key(MP.Read, "read", "127"))
  
  def testXML = {
    val xml = ring.toXML
    val ring2 = KeyRing.fromXML(xml)
    
    val keys1 = ListSet.empty[Key] ++ ring.keys
    val keys2 = ListSet.empty[Key] ++ ring2.keys
    
    assertTrue(keys1 == keys2)
  }
}
