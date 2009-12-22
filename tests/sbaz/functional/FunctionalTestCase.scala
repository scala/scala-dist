/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  James Matlik
 */

// $Id$
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
    directory(rootDir.split("/").toList.tail)
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
    val fileSep = System.getProperty("file.separator")
    val pathSep = System.getProperty("path.separator")
    val isWin: Boolean = System.getProperty("os.name") startsWith "Windows"
    val javaCmd = {
      val javaHome: String = System.getProperty("java.home") 
      val javaExec: String = if(isWin) "java.exe" else "java"
      val rel = relfile("bin", javaExec)
      rel.relativeTo( new File(javaHome) ).toString
    }
    val sbazJar = {
      val rel = relfile("misc", "sbaz", "sbaz.jar")
      rel.relativeTo(managedDir).toString
    }
    val scalaLibJar = {
      val rel = relfile("misc", "sbaz", "scala-library.jar")
      rel.relativeTo(managedDir).toString
    }

    assertTrue( new File(javaCmd) exists)
    assertTrue( new File(sbazJar) exists)
    assertTrue( new File(scalaLibJar) exists)

    def wrap(s: String) = "\"" + s + "\""
    val classpath = wrap(sbazJar + pathSep + scalaLibJar)
    val scalaHome = "-Dscala.home=" + wrap( managedDir.relativeTo(root).toString )
    val javaArgs =  "-Denv.classpath=" :: "-Denv.emacs=" :: {
      if (asyncDownload == true) "-Dsbaz.download.maxWorkers=2" :: Nil
      else Nil
    }
    val shellCmd = wrap(javaCmd) :: "-Xmx256M" :: "-Xms16M" :: "-cp" :: 
          classpath :: scalaHome :: javaArgs :::
          "sbaz.clui.CommandLine" :: cmd :: Nil  mkString("", " ", "")
    //println("shell command: " + shellCmd)
    scala.tools.nsc.io.Process(shellCmd)
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
