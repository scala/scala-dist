import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtNativePackager.{Windows, Debian, Rpm}

object Versioning {
  def settings: Seq[Setting[_]] = Seq(
    Windows / version := makeWindowsVersion(version.value),
    Debian / version  := toDebianVersion((Windows / version).value),
    Rpm / version     := toRpmVersion((Windows / version).value))

  private def rpmBuild(version:String): String = version split "\\." match {
    case Array(_,_,_, b) => b
    case _ => "1"
  }

  private def toRpmVersion(version:String): String = version split "\\." match {
    case Array(m,n,b,_*) => s"$m.$n.$b"
    case _ => version
  }

  private def toDebianVersion(version:String): String = version split "\\." match {
    case Array(m,n,b,z) => s"$m.$n.$b-$z"
    case _ => version
  }

  // This is a complicated means to convert maven version numbers into monotonically increasing windows versions.
  private def makeWindowsVersion(version: String): String = {
    val Majors     = """(\d+).(\d+).(\d+)(-.*)?""".r
    val Rcs        = """(-\d+)?-RC(\d+)""".r
    val Milestones = """(-\d+)?-M(\d+)""".r
    val BuildNum   = """-(\d+)""".r

    def calculateNumberFour(buildNum: Int = 0, rc: Int = 0, milestone: Int = 0) =
      if (rc > 0 || milestone > 0) (buildNum)*400 + rc*20  + milestone
      else (buildNum+1)*400 + rc*20  + milestone

    version match {
      case Majors(major, minor, bugfix, rest) =>
        s"$major.$minor.$bugfix." + (rest match {
            case null                  => calculateNumberFour(0,0,0)
            case Milestones(null, num) => calculateNumberFour(0,0,num.toInt)
            case Milestones(bnum, num) => calculateNumberFour(bnum.drop(1).toInt,0,num.toInt)
            case Rcs(null, num)        => calculateNumberFour(0,num.toInt,0)
            case Rcs(bnum, num)        => calculateNumberFour(bnum.drop(1).toInt,num.toInt,0)
            case BuildNum(bnum)        => calculateNumberFour(bnum.toInt,0,0)
            case _                     => calculateNumberFour(0,0,0)
          })
      case x => x
    }
  }
}
