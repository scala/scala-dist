package sbaz;

import messages._;
import junit.framework._ ;
import java.net.URL;

class MessagesTest extends TestCase {
  def testReloadAddPackage = {
    val pack =
      AvailablePackageUtil.fromXML(<package>
            <name>foo</name>
            <version>1.0</version>
            <depends/>
            <description>an example package</description>
            <link>http://www.foo.org/downloads/foo-1.0.zip</link>
	</package>);

    val msg = AddPackage(pack);

    val xml = msg.toXML;
    val msg2 = AddPackageUtil.fromXML(xml);

    Assert.assertTrue(msg.pack.spec == msg2.pack.spec);
    Assert.assertTrue(msg.pack.link == msg2.pack.link);
  }
}
