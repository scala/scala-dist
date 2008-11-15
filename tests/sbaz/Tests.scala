package sbaz

import junit.framework._

object Tests {
    def main(args : Array[String]) : Unit = {
    junit.textui.TestRunner.run(suite)
  }
    
  def suite(): Test = {
    val suite = new TestSuite()
    suite.addTestSuite(classOf[sbaz.AvailablePackageTest])
    suite.addTestSuite(classOf[sbaz.AvailableListTest])
    suite.addTestSuite(classOf[sbaz.InstalledEntryTest])
    suite.addTestSuite(classOf[sbaz.InstalledListTest])
    suite.addTestSuite(classOf[sbaz.MessagesTest])
    suite.addTestSuite(classOf[sbaz.VersionTest])
    suite.addTestSuite(classOf[sbaz.FilenameTest])
    suite.addTestSuite(classOf[sbaz.UniverseTest])
    suite.addTestSuite(classOf[sbaz.keys.KeyRingTest])
    suite
  }
}
