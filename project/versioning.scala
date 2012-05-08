import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer


/** A trait with helpers to generate version numbers from a single property.  Has specific handling for scala versions so
 *  we end up with good native versions.
 */
trait Versioning {
  def getScalaVersionPropertyOr(default: String): String =
    Option(System.getProperty("scala.version")) getOrElse default

  /** This is a complicated means to convert maven version numbers into monotonically increasing windows versions. */
  def makeWindowsVersion(version: String): String = {
    val Majors = new scala.util.matching.Regex("(\\d+).(\\d+).(\\d+)(-.*)?")
    val Rcs = new scala.util.matching.Regex("(\\-\\d+)?\\-RC(\\d+)")
    val Milestones = new scala.util.matching.Regex("(\\-\\d+)?\\-M(\\d+)")
    val BuildNum = new scala.util.matching.Regex("\\-(\\d+)")

    def calculateNumberFour(buildNum: Int = 0, rc: Int = 0, milestone: Int = 0) = 
      if(rc > 0 || milestone > 0) (buildNum)*400 + rc*20  + milestone
      else (buildNum+1)*400 + rc*20  + milestone

    version match {
      case Majors(major, minor, bugfix, rest) => Option(rest) getOrElse "" match {
        case Milestones(null, num)            => major + "." + minor + "." + bugfix + "." + calculateNumberFour(0,0,num.toInt)
        case Milestones(bnum, num)            => major + "." + minor + "." + bugfix + "." + calculateNumberFour(bnum.drop(1).toInt,0,num.toInt)
        case Rcs(null, num)                   => major + "." + minor + "." + bugfix + "." + calculateNumberFour(0,num.toInt,0)
        case Rcs(bnum, num)                   => major + "." + minor + "." + bugfix + "." + calculateNumberFour(bnum.drop(1).toInt,num.toInt,0)
        case BuildNum(bnum)                   => major + "." + minor + "." + bugfix + "." + calculateNumberFour(bnum.toInt,0,0)
        case _                                => major + "." + minor + "." + bugfix + "." + calculateNumberFour(0,0,0)
      }
    }
  }

  def getRpmBuildNumber(version:String): String = version split "\\." match {
    case Array(_,_,_, b) => b
    case _ => "1"
  }

  def getRpmVersion(version:String): String = version split "\\." match {
    case Array(m,n,b,_*) => "%s.%s.%s" format (m,n,b)
    case _ => version
  }

  def getDebianVersion(version:String): String = version split "\\." match {
    case Array(m,n,b,z) => "%s.%s.%s-%s" format (m,n,b,z)
    case _ => version
  }
}

