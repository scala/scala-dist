package sbaz
import junit.framework._
import Assert._
import java.net.URL

class UniverseTest extends TestCase {
  def testSimpleNotation1 {
    val univ1 = Universe.fromString("""
      <overrideuniverse>
        <components>
          <simpleuniverse>
            <name>scala-dev</name>
            <location>http://scbaztmp.lexspoon.org:8006/scala-dev</location>
          </simpleuniverse>
          <simpleuniverse>
            <name>local-hacks</name>
            <location>http://localhost/sbaz/local-hacks</location>
          </simpleuniverse>
        </components>
      </overrideuniverse>""")

    val univ2 = Universe.fromString("""
        scala-dev http://scbaztmp.lexspoon.org:8006/scala-dev
        local-hacks http://localhost/sbaz/local-hacks
        """)

    assertTrue(univ1.toString + " vs. " + univ2.toString, 
	       univ1.toString == univ2.toString)
  }

  def testSimpleNotation2 {
    val univ1 =
      new SimpleUniverse(
	"scala-dev",
	new URL("http://scbaztmp.lexspoon.org:8006/scala-dev"))

    val univ2 = Universe.fromString("""
        scala-dev http://scbaztmp.lexspoon.org:8006/scala-dev
        """)

    assertTrue(univ1.toString == univ2.toString)
  }



  def testSimpleNotation3 {
    val univ1 = new EmptyUniverse

    val univ2 = Universe.fromString("""
        """)


    assertTrue(
      univ1.toString + " vs. " + univ2.toString,
      univ1.toString == univ2.toString)
  }

  def testSimpleNotationBad {
    var threw = false
    try {
      Universe.fromString("a http://foo.bar/sbaz def")
    } catch {
      case _:FormatError => threw = true
    }
    assertTrue(threw)
  }


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
