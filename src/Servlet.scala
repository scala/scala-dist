package sbaz;

import java.io._ ;
import javax.servlet.http._ ;
import javax.servlet._ ;
import scala.xml._ ;

// An Servlet interface for updating a simple universe that is
// stored in a directory.  This class merely converts between
// HTTP requests and XML.  The processing is done in class
// ServletRequestHandler.

class Servlet
extends HttpServlet {
  var config: ServletConfig = null;
  def context = config.getServletContext() ;

  override def init(config:ServletConfig) =  {
    super.init(config);
    this.config = config;

  }

  def handler: ServletRequestHandler = {
    // for some reason, the following computation does not work
    // when called from init().
    val dirname = context.getInitParameter("dirname").asInstanceOf[String];
    ServletRequestHandler.handlerFor(dirname);
  }

  def universe: Universe = handler.universe ;


  override def doPost (req:HttpServletRequest,
		       res:HttpServletResponse) =  
  {
    val reqXML = XML.load(req.getReader());
    val reqMesg = MessageUtil.fromXML(reqXML);
    val respMesg = handler.handleRequest(reqMesg);
    val respXML = respMesg.toXML;

    res.getWriter().write(respXML.toString());
  }

  // XXX GET's should also defer to the handler; move the code over there...
  override def doGet (req:HttpServletRequest,
		      res:HttpServletResponse) =  
  {
    val out = res.getWriter();
    out.println("This is a Scala Bazaar.  The bazaar descriptor is:");
    out.println(universe.toXML.toString());
  }
}
