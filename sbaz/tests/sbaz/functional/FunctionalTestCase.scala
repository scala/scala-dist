/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  James Matlik
 */

package sbaz.functional

import java.io.{File, FileNotFoundException}

import junit.framework._
import Assert._

import sbaz.Filename
import sbaz.util.RichFile._

trait FunctionalTestCase extends TestCase {
  val functionalTestDir: Filename = {
    val systemTemp = System.getProperty("java.io.tmpdir")
    val rootDir = System.getProperty("sbaz.functional.dir", systemTemp)
    //val sep = System.getProperty("file.separator")
    directory(rootDir.split('/').toList.flatMap(_.split('\\')).filter(_.length > 0))
  }

  val testName: String
  lazy val packageBuildDir = directory(packageRepoDir ::: testName :: Nil)
  val packageRepoDir = directory(functionalTestDir ::: "package-repo" :: Nil)
  val managedDir = directory(functionalTestDir ::: "managed-dir" :: Nil)
  
  def initDir(filename: Filename) = {
    val dir: File = filename
    if(!dir.exists) { 
      if (!dir.mkdirs()) {
        throw new FileNotFoundException("Could not create directory " + dir.getAbsolutePath)
      }
      true
    } else false
  }

  def execSbaz(cmd: String, asyncDownload: Boolean = false): scala.tools.nsc.io.Process = { //Iterable[String] = {
    import sbaz.util.RichFile._
    import scala.util.Properties.isWin
    val fileSep = System.getProperty("file.separator")
    val pathSep = System.getProperty("path.separator")
    val javaCmd = {
      val javaHome: String = System.getProperty("java.home") 
      val javaExec: String = if(isWin) "java.exe" else "java"
      val rel = relfile("bin", javaExec)
      rel.relativeTo( new File(javaHome) ).toString
    }
    /*
    val sbazJar = {
      val rel = relfile("misc", "sbaz", "sbaz.jar")
      rel.relativeTo(managedDir.toFile).toString
    }
    val scalaLibJar = {
      val rel = relfile("misc", "sbaz", "scala-library.jar")
      rel.relativeTo(managedDir.toFile).toString
    }
    */
    val scalabazaarsJar = {
      val rel = relfile("misc", "sbaz", "scala-bazaars.jar")
      rel.relativeTo(managedDir.toFile).toString
    }

    assertTrue("Java command '" + javaCmd + "' could not be found.", new File(javaCmd) exists)
    assertTrue("Scala Bazaars JAR '" + scalabazaarsJar + "' could not be found.", new File(scalabazaarsJar) exists)
    //assertTrue("Sbaz JAR '" + sbazJar + "' could not be found.", new File(sbazJar) exists)
    //assertTrue("Scala Lib JAR '" + scalaLibJar + "' could not be found.", new File(scalaLibJar) exists)

    def wrap(s: String) = "\"" + s + "\""
    //val classpath = wrap(sbazJar + pathSep + scalaLibJar)
    val classpath = wrap(scalabazaarsJar)
    val scalaHome = "-Dscala.home=" + wrap( managedDir.toFile.toString )
    val javaArgs =  "-Denv.classpath=" :: "-Denv.emacs=" :: {
      if (asyncDownload == true) "-Dsbaz.download.maxWorkers=2" :: Nil
      else Nil
    }
    val shellCmd = wrap(javaCmd) :: "-Xmx256M" :: "-Xms16M" :: "-cp" :: 
          classpath :: scalaHome :: javaArgs :::
          "sbaz.clui.CommandLine" :: cmd :: Nil  mkString("", " ", "")
    val wrappedCmd = if(isWin) "\" " + shellCmd + " \"" else shellCmd
    //println("shell command: " + wrappedCmd)
    scala.tools.nsc.io.Process(wrappedCmd)
  }
  
  def getStdout(proc: scala.tools.nsc.io.Process): String = {
    "stdout = [" + proc.stdout.mkString("\n") + "]"
  }

  def getStderr(proc: scala.tools.nsc.io.Process): String = {
    "stderr = [" + proc.stderr.mkString("\n") + "]"
  }

  def getOutput(proc: scala.tools.nsc.io.Process): String = {
    getStdout(proc) + "\n" + getStderr(proc)
  }

  def assertExists(f: File) {
    if (!f.exists) fail("File " + f.getPath + " does not exist as expected.")
  }
  
  def assertNotExists(f: File) {
    if (f.exists) fail("File " + f.getPath + " exists when it should not.")
  }

  def assertEndsWith(expected: String, actual: String) {
    if (!actual.endsWith(expected)) {
      val len = expected.length
      val actualPiece = 
        if (len < actual.length) {
          "shoretened to " + len + " chars ...[" + 
          actual.substring(actual.length - len) + "]"
        }
        else "[" + actual + "]"
      fail("Expected " + expected + "\nActual " + actualPiece)
    }
  }
  private var startTime = 0l
  private var setupTime = 0l
  private var publishTime = 0l
  
  def start() { startTime = System.currentTimeMillis; setupTime = startTime }
  def setupDone() { setupTime = System.currentTimeMillis }
  def publishDone() { publishTime = System.currentTimeMillis }
  val statMsg = "[%s]setup: %.2f sec, publish: %.2f sec, exec: %.2f sec, total: %.2f sec"
  def printStats() {
    val execTime = System.currentTimeMillis
    val setupDur = (setupTime - startTime)/1000.0
    val publishDur = (publishTime - setupTime)/1000.0
    val execDur = (execTime - setupTime)/1000.0
    val totalDur = (execTime - startTime)/1000.0
    println(statMsg.format(testName, setupDur, publishDur, execDur, totalDur))
  }
}
