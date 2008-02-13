/* SBaz -- Scala Bazaar
 * Copyright 2005-2008 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id: $

package sbaz

import java.net.URL
import scala.collection.immutable.ListSet
import junit.framework._
import Assert.assertTrue

class InstalledListTest extends TestCase {
  val fooEntry =
    new InstalledEntry(
        new Package("foo",
                    new Version("1.0"),
                    new ListSet(),
                    "a package for foo-ing"),
        List(Filename.reldirectory("lib"), Filename.relfile("lib", "foo.jar")))


  val barEntry =
    new InstalledEntry(
      new Package("bar",
                  new Version("1.0"),
                  new ListSet(),
                  "a package for bar-ing"),
      List(Filename.reldirectory("lib"), Filename.relfile("lib", "bar.jar")))

  val list = new InstalledList()
  list.add(fooEntry)
  list.add(barEntry)

  def testIncludesFile {
    assertTrue(list.entriesWithFile(Filename.relfile("blahblah")).isEmpty)

    val fooEnts = list.entriesWithFile(Filename.relfile("lib", "foo.jar"))
    assertTrue(fooEnts.length == 1)
    assertTrue(fooEnts(0).packageSpec == fooEntry.packageSpec)

    val libEnts = list.entriesWithFile(Filename.reldirectory("lib"))
    assertTrue(libEnts.length == 2)
  }
}
