package scbaz;

class Version(str:String) {
  // simple placeholder for versions.  Eventually it will parse
  // the string into a sequence of strings and compare with
  // lexigraphic ordering 

  override def toString() = str;

  // XXX is there a Comparable mixin?
  def < (v : Version) = {
    str.compareTo(v.toString) < 0
  }

  def equals (v : Version) = {
    str.equals(v.toString)
  }
}
