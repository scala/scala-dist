package scbaz;

import java.io._;
import javax.servlet.http._;
import javax.servlet._;

// An Servlet interface for updating a simple universe that is
// stored in a directory.  This class merely converts between
// HTTP requests and XML.  The processing is done in class
// ServletRequestHandler.

class Servlet
extends HttpServlet {
  var config:ServletConfig = null;
  def context = config.getServletContext() ;

  override def init(config:ServletConfig) =  {
    super.init(config);
    this.config = config;
  }

  override def doGet (req:HttpServletRequest,
		      res:HttpServletResponse) =
  {
    val reqXML = ...
    val handler = ...
    val respXML = handler.handleRequest(handler)

    val out = res.getWriter();
    out.print(respXML.toString());
  }
}
