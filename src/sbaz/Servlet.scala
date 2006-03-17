package sbaz;

import java.io._ ;
import javax.servlet.http._ ;
import javax.servlet._ ;
import scala.xml._ 
import sbaz.messages.NotOK

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
    val dirname = getInitParameter("dirname").asInstanceOf[String];
    ServletRequestHandler.handlerFor(dirname);
  }

  def universe: Universe = handler.universe ;


  override def doPost (req:HttpServletRequest,
		       res:HttpServletResponse) =  
  {
    val reqXML = XML.load(req.getReader());
    val respMesg = try {
      val reqMesg = MessageUtil.fromXML(reqXML)
      req.getSession.getServletContext.log((<req><message>{reqXML}</message></req>).toString)
      handler.handleRequest(reqMesg)
    } catch {
      case ex => {
        ex.printStackTrace
        Console.println("problem request: " + reqXML)
        NotOK(ex.toString)
      }
    }

    val respXML = respMesg.toXML;

    res.setContentType("text/plain");
    res.getWriter().write(respXML.toString());
  }

  // XXX GET's should also defer to the handler; move the code over there...
  override def doGet (req:HttpServletRequest,
		      res:HttpServletResponse) =  
  {
    res.setContentType("text/plain");
    val out = res.getWriter();
    out.print(handler.responseForGET);
  }
}
