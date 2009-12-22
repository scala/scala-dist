/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  James Matlik
 */

// $Id$

package sbaz.util

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
import org.mortbay.log.Log

import sbaz.SimpleUniverse
import sbaz.util.RichFile._

abstract class JettyBazaarRunner {

  val bazaarPort: Int
  val bazaarHost: String
  val bazaarRoot: String
  val bazaarDir: File
  val bazaarUniverseName: String
  val bazaarKeylessRequestsText = 
    """<keylessRequests> 
      |  <edit nameregex=".*"/>
      |  <read/>
      |</keylessRequests>
      |""".stripMargin
  def bazaarUrl = "http://%s:%d%s".format(bazaarHost, bazaarPort, bazaarRoot)
  lazy val bazaarUniverse = new SimpleUniverse(bazaarUniverseName, new URL(bazaarUrl))

  def setupServerDir() {
    if (!bazaarDir.exists()) {
      bazaarDir.mkdir()
      new File(bazaarDir, "keylessRequests").write(bazaarKeylessRequestsText)
      new File(bazaarDir, "universe").write(bazaarUniverse.toXML.toString)
    }
  }

  /**
   * Start the server and then execute the body of a closure. The 
   * @param body
   */
  def runServer( body: Server => Any ) {
    val initParams = new java.util.HashMap[String, String]()
    initParams.put("dirname", bazaarDir.getAbsolutePath)

    val server = new Server(bazaarPort)
    val context = new Context(server, "/", Context.SESSIONS)
    val servletHolder = new ServletHolder(new sbaz.Servlet())
    servletHolder.setInitParameters(initParams)
    context.addServlet(servletHolder, bazaarRoot)
    server.start();
    Log.info(bazaarUniverse.toString)

    // wait for the server to be available
    while( server.isStarting() ) Thread.sleep(1000)
    if (!server.isStarted() ) throw new RuntimeException("Server failed to start");
    
    // Do the work
    body(server)
    
    // Stop the server
    if (!server.isStopping() && !server.isStopped) server.stop()
    if (!server.isStopped()) server.join()
  }
}
