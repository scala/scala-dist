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

class Install_Error_MissingDepFromUniverse extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Install_Error_MissingDepFromUniverse"

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

    val pack = new Package(
        testName,
        new Version("1.0"),
        ListSet.empty + "NonexistentPackage",
        "Package for sbaz.functional." + testName + " test case")

    val prevPack = new Package(
        testName,
        new Version("0.9"),
        ListSet.empty,
        "Package for sbaz.functional." + testName + " test case")

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
      // Submit a dummy earlier version -- This should not be selected
      val availablePack1 = new AvailablePackage(prevPack, sbp.url)
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertEquals(OK(), res1)

      // Submit the actual package -- This should fail
      val availablePack2 = new AvailablePackage(pack, sbp.url);
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
      """planning to install: Install_Error_MissingDepFromUniverse/1.0
        |Installing...
        |Error: Action aborted due to broken dependencies.
        |	Install_Error_MissingDepFromUniverse/1.0 depends on:
        |		NonexistentPackage
        |""".stripMargin
    val actual = ret2.mkString("", "\n", "")
    assertEquals(expected, actual)
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
