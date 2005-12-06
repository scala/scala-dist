package sbaz;

import java.net.URL;
import java.io.File;
import scala.collection.immutable.ListSet;
import junit.framework._ ;
import Assert.assertTrue;

class InstalledListTest extends TestCase {
  val fooEntry =
    new InstalledEntry(
      "foo",
      new Version("1.0"),
      List(new File("lib/foo.jar")),
      new ListSet(),
      true);

  val barEntry =
    new InstalledEntry(
      "bar",
      new Version("1.0"),
      List(new File("lib/bar.jar")),
      new ListSet(),
      true);


  val list = new InstalledList();
  list.add(fooEntry);
  list.add(barEntry);



  def testIncludesFile = {
    assertTrue(list.entriesWithFile(new File("blahblah")).isEmpty);

    
    val fooEnts = list.entriesWithFile(new File("lib/foo.jar"));
    assertTrue(fooEnts.length == 1);
    assertTrue(fooEnts(0).packageSpec == fooEntry.packageSpec);

    val libEnts = list.entriesWithFile(new File("lib"));
    assertTrue(fooEnts.length == 2);
  }
}
