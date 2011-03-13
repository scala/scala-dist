/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import junit.framework._
import junit.framework.Assert._

class InstalledEntryTest extends TestCase {
  def testOldFormat {
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
        
    val newPack = InstalledEntry.fromXML(newXML)
    val oldPack = InstalledEntry.fromXML(oldXML)
    
    assertTrue(newPack.name == "foo")
    assertTrue(newPack.version == new Version("1.5"))
    assertTrue(newPack.description == "A footastic package")
    assertTrue(newPack.depends.contains("bar"))
    assertTrue(newPack.depends.contains("tasm"))
    assertTrue(newPack.files.contains(Filename.relfile("lib", "foo.jar")))
    assertTrue(newPack.files.contains(Filename.relfile("src", "foo", "Foo.scala")))
    
    assertTrue(oldPack.name == "foo")
    assertTrue(oldPack.version == new Version("1.5"))
    assertTrue(oldPack.depends.contains("bar"))
    assertTrue(oldPack.depends.contains("tasm"))
    assertTrue(oldPack.files.contains(Filename.relfile("lib", "foo.jar")))
    assertTrue(oldPack.files.contains(Filename.relfile("src", "foo", "Foo.scala")))
  }
}
