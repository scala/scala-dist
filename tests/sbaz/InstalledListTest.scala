package sbaz;

import java.net.URL;
import java.io.File;
import scala.collection.immutable.ListSet;
import junit.framework._ ;
import Assert.assertTrue;

class InstalledListTest extends TestCase {
  val fooEntry =
    new InstalledEntry(
        new Package("foo",
                    new Version("1.0"),
                    new ListSet(),
                    "a package for foo-ing"),
        List(new File("lib"), new File("lib/foo.jar")))


  val barEntry =
    new InstalledEntry(
      new Package("bar",
                  new Version("1.0"),
                  new ListSet(),
                  "a package for bar-ing"),
      List(new File("lib"), new File("lib/bar.jar")))

      
  val list = new InstalledList();
  list.add(fooEntry);
  list.add(barEntry);



  def testIncludesFile = {
    assertTrue(list.entriesWithFile(new File("blahblah")).isEmpty);

    val fooEnts = list.entriesWithFile(new File("lib/foo.jar"));
    assertTrue(fooEnts.length == 1);
    assertTrue(fooEnts(0).packageSpec == fooEntry.packageSpec);

    val libEnts = list.entriesWithFile(new File("lib"));
    assertTrue(libEnts.length == 2);
  }
  
  def testOldFormat = {
    val newXML =
      <installedpackage>
    		<package>
        	<name>foo</name>
          <version>1.5</version>
          <description>A footastic package</description>
          <depends><name>bar</name> <name>tasm</name></depends>
        </package>
        <files>
          <filename>lib/foo.jar</filename>
          <filename>src/foo/Foo.scala</filename>
        </files>
      </installedpackage>;

    val oldXML =
      <installedpackage>
        <name>foo</name>
        <version>1.5</version>
        <depends><name>bar</name> <name>tasm</name></depends>
        <files>
          <filename>lib/foo.jar</filename>
          <filename>src/foo/Foo.scala</filename>
        </files>
        <complete/>
      </installedpackage>;
        
    val newPack = InstalledEntryUtil.fromXML(newXML)
    val oldPack = InstalledEntryUtil.fromXML(oldXML)
    
    assertTrue(newPack.name == "foo")
    assertTrue(newPack.version == new Version("1.5"))
    assertTrue(newPack.description == "A footastic package")
    assertTrue(newPack.depends.contains("bar"))
    assertTrue(newPack.depends.contains("tasm"))
    assertTrue(newPack.files.contains(new File("lib/foo.jar")))
    assertTrue(newPack.files.contains(new File("src/foo/Foo.scala")))
    
    assertTrue(oldPack.name == "foo")
    assertTrue(oldPack.version == new Version("1.5"))
    assertTrue(oldPack.depends.contains("bar"))
    assertTrue(oldPack.depends.contains("tasm"))
    assertTrue(oldPack.files.contains(new File("lib/foo.jar")))
    assertTrue(oldPack.files.contains(new File("src/foo/Foo.scala")))
  }
}
