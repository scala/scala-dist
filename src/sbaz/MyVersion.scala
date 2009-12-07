/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz

/** A utility to load the version of sbaz via a properties file
  * included in the jar.
  */
object MyVersion {
  /** The name of the properties file */
  val propFilename = "/sbaz.version.properties"
    
  /** The version number of the jar this was loaded from, or
    * "(unknown)" if it cannot be determined.
    */
  val versionString: String = {
    val defaultString = "(unknown)"
    val stream = classOf[sbaz.Universe].getResourceAsStream(propFilename)
    if(stream == null) defaultString else {
      val props = new java.util.Properties
      props.load(stream)
      val major = props.getProperty("version.major")
      val minor = props.getProperty("version.minor")
      if ((major == null) || (minor == null))
        defaultString
      else
        major + "." + minor
    }
  }
}
