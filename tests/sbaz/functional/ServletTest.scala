/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.functional

import sbaz._
import sbaz.messages._
import junit.framework._
import java.net.URL
import scala.collection.immutable.ListSet

import Assert._

/** Tests for the server.  It assumes that a bazaar server is 
 *  available for testing.
 *
 *  @author Lex Spoon
 */
class ServletTest extends TestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
    
  //val serverLink = new URL("http://localhost:8006/testbaz")
  //val universe = new SimpleUniverse("testbaz", serverLink)


  // test that GET-ing from the URL succeeds
  def testGET {
    val connection = serverLink.openConnection()
    val in = connection.getInputStream()

    def lp() {
      val dat = new Array[Byte](1000)
      val n = in.read(dat)
      if (n >= 0)
	lp();
    }
    lp()

    in.close()
  }

  // test adding a package, checking it is there,
  // removing it, and testing it is gone
  def testAddRemove {
    // a package to post
    val bogo =
      new AvailablePackage(
	new Package(
	      "bogo",
	      new Version("1.0"),
	      ListSet.empty,
	      "a bogus package for testing"),
	new URL("http://www.where.ever/bogo-1.0.zip"));

    // post the package
    {
      val res = universe.requestFromServer(AddPackage(bogo))
      assertTrue(res == OK())
    }

    // make sure it is there
    {
      val avail = universe.retrieveAvailable()
      val pack = avail.packageWithSpec(bogo.spec).get

      assertTrue(pack.spec == bogo.spec)
    }


    // retract the package
    {
      val res = universe.requestFromServer(RemovePackage(bogo.spec))
      assertTrue(res == OK())
    }

    // make sure the package is gone
    {
      val avail = universe.retrieveAvailable()
      val pack = avail.packageWithSpec(bogo.spec)
      assertTrue(pack.isEmpty)
    }
  }

}
