package sbaz ;

import scala.xml._ ;

class EmptyUniverse extends Universe("empty",
				     "A universe with no packages.")
{
  def retrieveAvailable() = new AvailableList(Nil) ;
  override def simpleUniverses = List() ;
  override def toXML = <emptyuniverse/> ;
}
