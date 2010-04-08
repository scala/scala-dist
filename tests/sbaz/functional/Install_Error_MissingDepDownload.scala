/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  James Matlik
 */

// $Id$

package sbaz.functional

import sbaz._
import sbaz.messages._
import sbaz.util.RichFile._
import sbaz.util.Zip
import java.io.{File, FileNotFoundException}
import java.net.URL
import scala.collection.immutable.ListSet

import junit.framework.Assert._

class Install_Error_MissingDepDownload extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Install_Error_MissingDepDownload"

  def testInstall {
    start()
/*============================================================================*\
**                           Prepare the test case                            **
\*============================================================================*/
    val srcDir:Filename = directory(packageBuildDir ::: "src" :: Nil)

    // file1 is a simple text file
    val file1name = relfile("misc", testName, "file1.txt")
    val file1src = file1name.relativeTo(srcDir)

    // file2 is a JAR with pack200
    val file2TextName = relfile("lib", testName + "_file2.txt")
    val file2TextSrc = file2TextName.relativeTo(srcDir)
    val file2ZipName = relfile("lib", testName + "_file2.jar")
    val file2PackName = relfile("lib", testName +"_file2.pack")
    val file2ZipSrc = file2ZipName.relativeTo(srcDir)

    val descFilename = relfile("meta", "description")
    val descriptionFile = descFilename.relativeTo(srcDir)

    val sbp = new File(packageBuildDir, testName + ".sbp")
    val dneURL = new URL("http://" + Tests.bazaarHost + ":" + Tests.bazaarPort
      + "/dne.sbp")

    val pack = new Package(
        testName,
        new Version("1.0"),
        ListSet.empty + (testName + "_required"),
        "Package for sbaz.functional." + testName + " test case")

    val reqPack = new Package(
        testName + "_required",
        new Version("0.9"),
        ListSet.empty,
        "Depended upon package for sbaz.functional." + testName + " test case")

    // Make the files only if needed
    if(initDir(packageBuildDir)) {
      file1src.parent.mkdirs
      file1src.append("This is file #1")
      
      file2TextSrc.parent.mkdirs
      file2TextSrc.append("This is file #2")
      Zip.create(file2ZipSrc, srcDir, file2TextName :: Nil)
      val file2PackedSrc = file2ZipSrc.repack200.pack200
      

      descriptionFile.parent.mkdirs()
      descriptionFile.append(pack.toXML.toString)
      
      // Jar up into the sbp package file
      Zip.create(sbp, srcDir, file1name :: file2PackName :: descFilename :: Nil)
      assertExists(sbp)
    }
    setupDone()

/*============================================================================*\
**                       Publish the package to bazaar                        **
\*============================================================================*/
    {
      // Submit required package with broken link to sbp
      // Originally written with new URL("http://nowhere.com/dne.sbp"), but
      // took over 15 seconds for host lookup to timeout.
      val availablePack1 = new AvailablePackage(reqPack, dneURL)
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertEquals(OK(), res1)

      // Submit the actual package -- This should fail
      val availablePack2 = new AvailablePackage(pack, sbp.url)
      val res2 = universe.requestFromServer(AddPackage(availablePack2))
      assertEquals(OK(), res2)
    }
    publishDone()

/*============================================================================*\
**                     Install package in Managed Directory                   **
\*============================================================================*/
    val universeFile = file(srcDir ::: "universe" :: Nil)
    universeFile.write(Tests.bazaarUniverse.toXML.toString)
    val ret1: scala.tools.nsc.io.Process = execSbaz("setuniverse  \"" 
      + universeFile.toFile + "\"")
    assertEquals(getOutput(ret1), 0, ret1.waitFor)
    val ret2: scala.tools.nsc.io.Process = execSbaz("install " + testName)
    assertEquals(getOutput(ret2), 1, ret2.waitFor)
    
    val expected = 
      """Downloading: http://localhost:8006/dne.sbp
        |Failed: java.io.FileNotFoundException: http://localhost:8006/dne.sbp
        |Error: Required dependencies could not be downloaded:
        |	Install_Error_MissingDepDownload_required/0.9: Fail: java.io.FileNotFoundException: http://localhost:8006/dne.sbp
        |""".stripMargin
    val actual = ret2.mkString("", "\n", "")
    new File("/tmp/actual").write(actual)
    new File("/tmp/expected").write(expected)
    assertEndsWith(expected, actual)
/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    val file1dest: File = file1name.relativeTo(managedDir)
    val file2ZipDest: File = file2ZipName.relativeTo(managedDir)
    assertNotExists(file1dest)
    assertNotExists(file2ZipDest)
    printStats()
  }
}
