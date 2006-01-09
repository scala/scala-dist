package sbaz;
import junit.framework._;
import Assert._;

class VersionTest extends TestCase {
  def testOrder = {
    val stringsInOrder =
      List("",
	   "abc",
	   "abcd",
	   "abd",
	   "1",
	   "1.1",
	   "1.1a",
	   "1.1a2",
	   "1.1a100",
	   "1.1.5",
	   "1.2",
	   "1.2.",
	   "2",
	   "12");
    val inOrder = stringsInOrder.map(s => new Version(s));
	   
	   
    val stringsOutOfOrder = stringsInOrder.reverse;
    val outOfOrder = stringsOutOfOrder.map(s => new Version(s));

    val sorted = outOfOrder.sort((v1,v2) => v1 < v2);

    //Console.println("sorted = " + sorted);

    assertTrue(sorted == inOrder);
  }

  def testOtherOps = {
    assertTrue(new Version("1") < new Version("2"));
    assertTrue(new Version("1") <= new Version("2"));
    assertTrue(new Version("2") > new Version("1"));
    assertTrue(new Version("1.5") == new Version("1.5"));
  }

  def testToString = {
    assertTrue((new Version("1.5a")).toString() == "1.5a");
    assertTrue((new Version("")).toString() == "");
  }
}
