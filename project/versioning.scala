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

  def versionSettings(k: SettingKey[String]): Seq[Setting[_]] = Seq(
     version <<= k,
     version in Windows <<= version apply makeWindowsVersion,
     version in Rpm <<= (version in Windows) apply getRpmVersion,
     rpmRelease <<= (version in Windows) apply getRpmBuildNumber,
     version in Debian <<= (version in Windows) apply getDebianVersion
  )

  def getScalaVersionOr(libJar: File, default: String): String =
    loadScalaVersion(libJar) getOrElse getScalaVersionPropertyOr(default)

  def getScalaVersionPropertyOr(default: String): String =
    Option(System.getProperty("scala.version")) getOrElse default

  def loadScalaVersion(libJar: File): Option[String] = try {
    def readStream(stream: java.io.InputStream): Option[String] =
      try {
			  val props = new java.util.Properties
        props.load(stream)
        Option(props.getProperty("maven.version.number"))
      } finally stream.close()

    import java.util.jar.JarFile
    val jar = new JarFile(libJar)
    for {
      e <- Option(jar getEntry "library.properties")
      version <- readStream(jar getInputStream e)
    } yield version
  } catch {
    case e: Exception => None
  }

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
      case x => x
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
object Versioning extends Versioning

