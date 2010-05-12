/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import junit.framework._ 
import java.net.URL
import scala.collection.immutable.ListSet

class AvailablePackageTest extends TestCase {
  def testAltXML {
    val xml =
        <package>
            <name>foo</name>
            <version>1.0</version>
            <depends/>
            <description>an example package</description>
            <link>http://www.foo.org/downloads/foo-1.0.zip</link>
	</package>;

    val decoded = AvailablePackageUtil.fromXML(xml);

    Assert.assertTrue(decoded.pack.name == "foo");
    Assert.assertTrue(decoded.pack.version == new Version("1.0"));
    Assert.assertTrue(decoded.pack.depends.isEmpty);
    Assert.assertTrue(decoded.pack.description.startsWith("an example"));
    Assert.assertTrue(decoded.link ==
		      new URL("http://www.foo.org/downloads/foo-1.0.zip"));

  }

  def testXMLandBack {
    val pack =
      new AvailablePackage(
	new Package("foo",
		    new Version("1.0"),
		    new ListSet(),
		    "an example package"),
	new URL("http://www.foo.org/downloads/foo-1.0.zip"));

    val xml = pack.toXML;
    val pack2 = AvailablePackageUtil.fromXML(xml);

    Assert.assertTrue(pack.spec == pack2.spec);
    Assert.assertTrue(pack.link == pack2.link);
  }
}

