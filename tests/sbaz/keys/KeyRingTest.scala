package sbaz.keys
import junit.framework._
import junit.framework.Assert._
import scala.collection.immutable.ListSet
import sbaz.keys.{msgpatt=>MP}

class KeyRingTest extends TestCase {
  val ring = new KeyRing
  ring.addKey(Key(MP.EditKeys, "editkeys", "1234"))
  ring.addKey(Key(MP.Edit(".*"), "editall", "1235"))
  ring.addKey(Key(MP.Edit("finger"), "edit-finger", "1236"))
  ring.addKey(Key(MP.Read, "read", "127"))
  
  //ring.add(....  add a bunch of keys to play with...
	def testXML = {
    val xml = ring.toXML
    val ring2 = KeyRing.fromXML(xml)
    
    val keys1 = new ListSet[Key]().incl(ring.keys)
    val keys2 = new ListSet[Key]().incl(ring2.keys)
    
    assertTrue(keys1 == keys2)
  }
}
