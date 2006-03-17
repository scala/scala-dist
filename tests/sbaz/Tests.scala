package sbaz;

import junit.framework._;

object Tests {
  def suite(): Test = {
    val suite = new TestSuite();
    suite.addTestSuite(Class.forName("sbaz.AvailablePackageTest"))
    suite.addTestSuite(Class.forName("sbaz.AvailableListTest"))
    suite.addTestSuite(Class.forName("sbaz.InstalledEntryTest"))
    suite.addTestSuite(Class.forName("sbaz.InstalledListTest"))
    suite.addTestSuite(Class.forName("sbaz.MessagesTest"))
    suite.addTestSuite(Class.forName("sbaz.VersionTest"))
    suite.addTestSuite(Class.forName("sbaz.FilenameTest"))
    suite.addTestSuite(Class.forName("sbaz.keys.KeyRingTest"));
    suite
  }
}
