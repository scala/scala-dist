package scbaz;

class Version(str:String) {
  // simple placeholder for versions.  Eventually it will parse
  // the string into a sequence of strings and compare with
  // lexigraphic ordering.  perhaps the Debian version scheme
  // could be copied directly....

  override def toString() = str;

  // XXX is there a Comparable mixin?
  def < (v : Version) = {
    str.compareTo(v.toString) < 0
  }

  def > (v : Version) = {
    str.compareTo(v.toString) > 0
  }

  def >= (v : Version) = {
    str.compareTo(v.toString) >= 0
  }


  def equals (v : Version) = {
    str.equals(v.toString)
  }
}
