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

class Install_SimpleDependency extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Install_SimpleDependency"

  def testInstall {
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
      assertTrue(res1 == OK())

      val availablePack2 = new AvailablePackage(pack2, sbp2.url);
      val res2 = universe.requestFromServer(AddPackage(availablePack2))
      assertTrue(res2 == OK())
    }
    publishDone()

/*============================================================================*\
**                     Install package in Managed Directory                   **
\*============================================================================*/
    val universeFile = file(srcDir ::: "universe" :: Nil)
    universeFile.write(Tests.bazaarUniverse.toXML.toString)
    val ret1: scala.tools.nsc.io.Process = execSbaz("setuniverse  \"" 
      + universeFile.relativeTo(new File("")) + "\"")
    //ret1.foreach( x => println(x) )
    assertEquals(0, ret1.waitFor)

    // Only install the package2, letting dependency resolution pull package1
    val ret2: scala.tools.nsc.io.Process = execSbaz("install " + testName)
    assertEquals(0, ret2.waitFor)
    var downloads = 0
    ret2.foreach { 
      x => if (x contains "Downloading:") downloads = downloads + 1
      //println(x)
    }
    assertEquals(2, downloads)
/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    val file1dest = file1name.relativeTo(managedDir)
    val file2dest = file2name.relativeTo(managedDir)
    assertEquals(file1src.md5, file1dest.md5)
    assertEquals(file2src.md5, file2dest.md5)
    printStats()
  }
}
