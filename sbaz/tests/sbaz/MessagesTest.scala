/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz

import messages._
import junit.framework._

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

    val msg = AddPackage(pack)

    val xml = msg.toXML
    val msg2 = AddPackageUtil.fromXML(xml)

    Assert.assertTrue(msg.pack.spec == msg2.pack.spec)
    Assert.assertTrue(msg.pack.link == msg2.pack.link)
  }
  
  // def toFromXML :  test loading and saving a bunch of messages....
}
