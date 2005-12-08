package sbaz;

import junit.framework._;

object Tests {
  def suite(): Test = {
    val suite = new TestSuite();
    suite.addTestSuite(new AvailablePackageTest().getClass());
    suite.addTestSuite(new AvailableListTest().getClass());
    suite.addTestSuite(new InstalledListTest().getClass());
    suite.addTestSuite(new MessagesTest().getClass());
    suite
  }
}
