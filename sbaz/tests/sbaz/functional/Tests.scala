/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  James Matlik
 */


package sbaz.functional

import java.io.File
import java.net.URL

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import junit.framework.TestCase
import junit.framework.TestSuite
import junit.textui.TestRunner

import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.ServletHolder

import sbaz.SimpleUniverse
import sbaz.util.JettyBazaarRunner
import sbaz.util.RichFile._

object Tests extends JettyBazaarRunner {
 
  def suite = {
    val suite = new TestSuite("sbaz functional tests")
    suite.addTestSuite(classOf[ServletTest])
    suite.addTestSuite(classOf[Install_SinglePackage])
    suite.addTestSuite(classOf[Install_SimpleDependency])
    suite.addTestSuite(classOf[Install_PreinstalledDependency])
    suite.addTestSuite(classOf[Install_Error_MissingDepFromUniverse])
    suite.addTestSuite(classOf[Install_Error_MissingDepDownload])
    suite.addTestSuite(classOf[Install_Error_MissingDepDownload_Async])
    suite.addTestSuite(classOf[Install_Error_PackageContentCollision])
    suite.addTestSuite(classOf[Upgrade])
    suite.addTestSuite(classOf[Upgrade_ContentMigration])
    suite.addTestSuite(classOf[Upgrade_Error_MissingDep])
    suite.addTestSuite(classOf[Remove_SimplePackage])
    suite.addTestSuite(classOf[Remove_Error_BrokenDep])
    suite
  }

  val bazaarPort = 8006
  val bazaarHost = "localhost"
  val bazaarRoot = "/testbaz"
  val bazaarDir = {
    val systemTemp = System.getProperty("java.io.tmpdir")
    val rootDir = System.getProperty("sbaz.functional.dir", systemTemp)
    new File(rootDir, "bazaar")
  }
  val bazaarUniverseName = "testbaz Bazaar"

  def main(args : Array[String]) : Unit = {
    setupServerDir()
    runServer { server => 
      TestRunner.run(suite)
    }
  }
}
