/* SBaz -- Scala Bazaar
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id: $

package sbaz

import junit.framework._
import java.net.URL

class AvailableListTest extends TestCase {
  def testAltXML {
    val xml =
      <packageset>
        <package>
            <name>foo</name>
            <version>1.0</version>
            <depends/>
            <description>an example package</description>
            <link>http://www.foo.org/downloads/foo-1.0.zip</link>
	</package>
        <package>
            <name>foo</name>
            <version>1.2</version>
            <depends/>
            <description>an example package</description>
            <link>http://www.foo.org/downloads/foo-1.2.zip</link>
	</package>
      </packageset>;

    val decoded = AvailableListUtil.fromXML(xml);
    val Some(entry) = decoded.newestNamed("foo");

    Assert.assertTrue(entry.pack.name == "foo");
    Assert.assertTrue(entry.pack.version == new Version("1.2"));
    Assert.assertTrue(entry.pack.depends.isEmpty);
    Assert.assertTrue(entry.pack.description.startsWith("an example"));
    Assert.assertTrue(entry.link ==
		      new URL("http://www.foo.org/downloads/foo-1.2.zip"));

  }

}

