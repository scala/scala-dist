package scbaz;

import scala.collection.mutable.Set ;
import scala.collection.mutable.HashSet ;

class ManagedDirectory(directory : java.io.File) {

//  var universe : Universe;

  val installed : Set[Package]  =  new HashSet();
  val available : Set[Package] = new HashSet();

  def setAvailable() = {  }

  def install(pack : Package) = {  }
}
