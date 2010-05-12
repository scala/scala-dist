/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


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
  
  def testBreakingChange {
	val a = new Package("A", new Version("1.0"), new ListSet(), "I do not depend on anything")
	val depsOnA = new ListSet()+"A"
	val b = new Package("B", new Version("1.0"), depsOnA, "I depend on A")
	val c = new Package("C", new Version("1.0"), depsOnA, "I also depend on A")
    val installedA = new InstalledEntry(a, List(Filename.reldirectory("lib"), Filename.relfile("lib", "A.jar")))
    val installedB = new InstalledEntry(b, List(Filename.reldirectory("lib"), Filename.relfile("lib", "B.jar")))
    val installedC = new InstalledEntry(c, List(Filename.reldirectory("lib"), Filename.relfile("lib", "C.jar")))
	val list = new InstalledList()
	
    // Dependency breaking add to an empty install
    val availableB = new AvailablePackage(b, new URL("http://nowhere.com/b.sbp"))
	val additionB = ProposedChanges.AdditionFromNet(availableB)
	val brokenB = list.identifyBreakingChanges((additionB::Nil).toSeq)
	assertTrue(brokenB.size == 1)
	assertTrue(brokenB.contains( (b, depsOnA) ))
	
	// Existing broken dependency does not show up as new
	list.add(installedC)
	val brokenB2 = list.identifyBreakingChanges((additionB::Nil).toSeq)
	assertTrue(brokenB2.size == 1)
	assertTrue(brokenB2.contains( (b, depsOnA) ))
	
	// Dependency breaking removal
    list.add(installedA)
    list.add(installedB)
    val removeA = new ProposedChanges.Removal(a.spec)
	val brokenRemoval = list.identifyBreakingChanges((removeA::Nil).toSeq)
	assertTrue(brokenRemoval.size == 2)
	assertTrue(brokenRemoval.contains( (b, depsOnA) ))
	assertTrue(brokenRemoval.contains( (c, depsOnA) ))

  }
}
