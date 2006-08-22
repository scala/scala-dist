/* SBAZ -- the Scala Bazaar
 * Copyright 2005-2006 LAMP/EPFL
 * @author  Lex Spoon
 */
// $Id$

package sbaz

// A component of a Version.  A Version number is divided into
// a sequence of components.  Each component is either a sequence
// of letters, a sequence of numbers, or a sequence of symbols.
// Versions are compared lexicographically by their list of components.
sealed class VersionComp;
case class VCAlpha(val str: String) extends VersionComp {
  override def toString() = str
}
case class VCNum(val num: int) extends VersionComp {
  override def toString(): String = num.toString()
}
case class VCSym(val syms: String) extends VersionComp {
  override def toString() = syms
}

class Version(val comps: List[VersionComp]) extends Ordered[Version] {
  def this(str: String) = this(VersionUtil.componentsFrom(str))

  override def toString(): String = {
    val buf = new StringBuffer();
    for(val vc <- comps) {
      buf.append(vc)
    }
    buf.toString()
  }


  override def equals (v : Any): Boolean = {
    (v.isInstanceOf[Version] &&
     (compareTo(v.asInstanceOf[Version]) == 0))
  }

  def compare(that: Version): int = {
    def cmpComps(comps1: List[VersionComp],
                 comps2: List[VersionComp]): int =
      comps1 match {
        case Nil =>
          comps2 match {
            case Nil => 0
            case _ => -1
          }
        case c1::rest1 =>
          comps2 match {
            case Nil => 1
            case c2::rest2 =>
              VersionUtil.compareComps(c1, c2) match {
                case 0 => cmpComps(rest1, rest2)
                case ord => ord
              }
          }
      }

    cmpComps(comps, that.comps)
  }
}

object VersionUtil {
  // Compare two version components.  Alpha's come first,
  // followed by numbers, followed by symbols.  The return
  // value is as for Ordered.compareTo() .
  def compareComps(c1: VersionComp, c2: VersionComp): int = {
    Pair(c1, c2) match {
      case Pair(VCAlpha(s1), VCAlpha(s2)) =>
        s1.compareTo(s2)
      case Pair(VCNum(n1), VCNum(n2)) =>
        if(n1 < n2) -1 else
        if(n1 > n2) 1 else 0
      case Pair(VCSym(s1), VCSym(s2)) =>
        s1.compareTo(s2)
      case Pair(VCAlpha(_), VCNum(_)) => -1
      case Pair(VCAlpha(_), VCSym(_)) => -1
      case Pair(VCNum(_), VCAlpha(_)) => 1
      case Pair(VCNum(_), VCSym(_)) => -1
      case Pair(VCSym(_), VCAlpha(_)) => 1
      case Pair(VCSym(_), VCNum(_)) => 1  // XXX apparent compiler
                                          // bug if I do _:VCSym instead
                                          // of VCSym(_)
    }
  }


  // parse a version string into a list of version components
  def componentsFrom(str: String) = {
    def ctype(c: char) = 
      if(Character.isLetter(c))
        'alpha
      else if((c >= '0') && (c <= '9'))
        'num
      else
        'sym

    // the growing list of components, in reverse
    var rcomps: List[VersionComp] = Nil;

    // add a component from the given range in the string
    def addvc(start: int, end: int) = {
      val substr = str.substring(start, end+1)
      val vc = ctype(substr.charAt(0)) match {
        case 'num => VCNum(Integer.parseInt(substr))
        case 'alpha => VCAlpha(substr)
        case 'sym => VCSym(substr)
      }
      rcomps = vc :: rcomps;
    }

    // The following loop moves a window (start, end) through
    // str.  At the entry, the substring start..end is assumed
    // to all have the same character types.  Extend 'end' if
    // possible.  If not, register a new component and then
    // start on a new one.
    def lp(start: int, end: int): Unit = {
      if (end+1 >= str.length()) {
        // reached the end of the string
        if (end >= start)
          addvc(start, end)
      }
      else {
	if(start > end)
	  lp(start, end+1)  // starting a new component
	else {
	  if(ctype(str.charAt(start)) == ctype(str.charAt(end+1)))
	    lp(start, end+1)  // same ctype -- extend the comp 1 more
	  else {
	    // different ctype; start a new component
	    addvc(start, end) 
	    lp(end+1, end)
	  }
	}
      }
    }
    lp(0, -1)

    rcomps.reverse
  }
}
