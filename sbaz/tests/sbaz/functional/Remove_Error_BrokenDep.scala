/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  James Matlik
 */


package sbaz.functional

import sbaz._
import sbaz.messages._
import sbaz.util.RichFile._
import sbaz.util.Zip
import java.io.{File, FileNotFoundException}
import java.net.URL
import scala.collection.immutable.ListSet

import junit.framework.Assert._

class Remove_Error_BrokenDep extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Remove_Error_BrokenDep"

  def testRemove {
    start()
/*============================================================================*\
**                           Prepare the test case                            **
\*============================================================================*/
    val srcDir:Filename = directory(packageBuildDir ::: "src" :: Nil)

    // file1 is a simple text file for package 1
    val file1name = relfile("misc", testName, "file1.txt")
    val file1src = file1name.relativeTo(srcDir)
    val descFilename1 = relfile("meta", "description1")
    val descriptionFile1 = descFilename1.relativeTo(srcDir)

    // file2 is a simple text file for package 2
    val file2name = relfile("misc", testName, "file2.txt")
    val file2src = file2name.relativeTo(srcDir)
    val descFilename2 = relfile("meta", "description2")
    val descriptionFile2 = descFilename2.relativeTo(srcDir)

    // The packages containing the files
    val sbp1 = new File(packageBuildDir, testName + "1_required.sbp")
    val sbp2 = new File(packageBuildDir, testName + "2.sbp")

    val pack1 = new Package(
        testName + "_required",
        new Version("1.0"),
        ListSet.empty,
        "Package1 for sbaz.functional." + testName + " test case")

    val pack2 = new Package(
        testName,
        new Version("1.0"),
        ListSet.empty + (testName + "_required"),
        "Package2 for sbaz.functional." + testName + " test case")

    // Make the files only if needed
    if(initDir(packageBuildDir)) {
      file1src.parent.mkdirs
      file1src.append("This is file #1. It is depended upon.")
      descriptionFile1.parent.mkdirs()
      descriptionFile1.append(pack1.toXML.toString)
      Zip.create(sbp1, srcDir, file1name :: descFilename1 :: Nil)
      
      file2src.parent.mkdirs
      file2src.append("This is file #2")
      descriptionFile2.parent.mkdirs()
      descriptionFile2.append(pack2.toXML.toString)
      Zip.create(sbp2, srcDir, file2name :: descFilename2 :: Nil)
    }
    setupDone()

/*============================================================================*\
**                       Publish the package to bazaar                        **
\*============================================================================*/
    {
      val availablePack1 = new AvailablePackage(pack1, sbp1.url);
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertEquals(OK(), res1)

      val availablePack2 = new AvailablePackage(pack2, sbp2.url);
      val res2 = universe.requestFromServer(AddPackage(availablePack2))
      assertEquals(OK(), res2)
    }
    publishDone()

/*============================================================================*\
**                     Install package in Managed Directory                   **
\*============================================================================*/
    {
      val universeFile = file(srcDir ::: "universe" :: Nil)
      universeFile.write(Tests.bazaarUniverse.toXML.toString)
      val ret1: scala.tools.nsc.io.Process = execSbaz("setuniverse  \"" 
        + universeFile.toFile + "\"")
      //ret1.foreach( x => println(x) )
      assertEquals(getOutput(ret1), 0, ret1.waitFor)

      // Only install the package2, letting dependency resolution pull package1
      val ret2: scala.tools.nsc.io.Process = execSbaz("install " + testName)
      assertEquals(getOutput(ret2), 0, ret2.waitFor)
      var downloads = 0
      ret2.foreach { 
        x => if (x contains "Downloading:") downloads = downloads + 1
        //println(x)
      }
      assertEquals("Unexpected number of downloads", 2, downloads)
    }
    
/*============================================================================*\
**                      Validate setup in Managed Directory                   **
\*============================================================================*/
    val file1dest = file1name.relativeTo(managedDir)
    val file2dest = file2name.relativeTo(managedDir)
    assertEquals(file1src.md5, file1dest.md5)
    assertEquals(file2src.md5, file2dest.md5)
    
/*============================================================================*\
**                 Attempt remove required from Managed Directory             **
\*============================================================================*/
    {
      val ret1: scala.tools.nsc.io.Process = execSbaz("remove " + testName + "_required")
      assertEquals(getOutput(ret1), 1, ret1.waitFor)
      val ret2: scala.tools.nsc.io.Process = execSbaz("installed")
      assertEquals(getOutput(ret2), 0, ret2.waitFor)
      var hits = 0
      ret2.foreach(line => if (line.trim.equals(pack1.spec.toString)) hits = hits + 1)
      assertEquals("Unexpected number of downloads", 1, hits)
    }    
    assertEquals(file1src.md5, file1dest.md5)
    assertEquals(file2src.md5, file2dest.md5)
    printStats()
  }
}
