/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
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

class Upgrade_Error_MissingDep extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Upgrade_Error_MissingDep"

  def testUpgrade {
    start()
/*============================================================================*\
**                           Prepare the test case                            **
\*============================================================================*/
    val srcDir:Filename = directory(packageBuildDir ::: "src" :: Nil)

    // file1 is a simple text file for package 1
    val file1name = relfile("misc", testName, "file1.txt")
    val file1src = file1name.relativeTo(srcDir)

    // file2 is a simple text file for package 2
    val file2name = relfile("misc", testName, "file2.txt")
    val file2src = file2name.relativeTo(srcDir)

    // file3 is a simple text file for both packages
    val file3name = relfile("misc", testName, "file3.txt")
    val file3src = file3name.relativeTo(srcDir)
    
    // The packages containing the files
    val sbp1 = new File(packageBuildDir, testName + "1.sbp")
    val sbp2 = new File(packageBuildDir, testName + "2.sbp")

    val pack1 = new Package(
        testName,
        new Version("1.0"),
        ListSet.empty,
        "Package 1.0 for sbaz.functional." + testName + " test case")

    val pack2 = new Package(
        testName,
        new Version("1.1"),
        ListSet.empty + "DoesNotExist",
        "Package 1.1 for sbaz.functional." + testName + " test case")

    // Make the files only if needed
    if(initDir(packageBuildDir)) {
      file1src.parent.mkdirs
      file1src.append("This is file #1. It is depended upon.")
      file3src.parent.mkdirs
      file3src.append("This file should be in both packages.")
      Zip.create(sbp1, srcDir, file1name :: file3name :: Nil)
      
      file2src.parent.mkdirs
      file2src.append("This is file #2")
      Zip.create(sbp2, srcDir, file2name :: file3name :: Nil)
    }
    setupDone()

/*============================================================================*\
**                       Publish the package to bazaar                        **
\*============================================================================*/
    {
      val availablePack1 = new AvailablePackage(pack1, sbp1.url);
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertEquals(OK(), res1)
    }
    publishDone()

/*============================================================================*\
**                     Install package in Managed Directory                   **
\*============================================================================*/
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
    assertEquals("Unexpected number of downloads", 1, downloads)

    // Upgrade should result in a no-op 
    val ret3: scala.tools.nsc.io.Process = execSbaz("upgrade")
    assertEquals(getOutput(ret3), 0, ret3.waitFor)
    downloads = 0
    ret3.foreach { 
      x => if (x contains "Downloading:") downloads = downloads + 1
      //println(x)
    }
    assertEquals("Unexpected number of downloads", 0, downloads)

/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    val file1dest: File = file1name.relativeTo(managedDir)
    val file2dest: File = file2name.relativeTo(managedDir)
    val file3dest: File = file3name.relativeTo(managedDir)
    assertExists(file1dest)
    assertNotExists(file2dest)
    assertExists(file3dest)
    assertEquals(file1src.md5, file1dest.md5)
    assertEquals(file3src.md5, file3dest.md5)

/*============================================================================*\
**                   Publish the package upgrade to bazaar                    **
\*============================================================================*/
    val availablePack2 = new AvailablePackage(pack2, sbp2.url);
    val res2 = universe.requestFromServer(AddPackage(availablePack2))
    assertEquals(OK(), res2)

    // Upgrade should result in a no-op 
    //execSbaz("update").foreach (x => println(x))
    //execSbaz("available").foreach (x => println(x))
    val ret4: scala.tools.nsc.io.Process = execSbaz("upgrade")
    assertEquals(getOutput(ret4), 1, ret4.waitFor)
    val actual = ret4.mkString("", "\n", "")
    val expected = 
    """Planning to install Upgrade_Error_MissingDep/1.1...
      |Installing...
      |Error: Action aborted due to broken dependencies.
      |	Upgrade_Error_MissingDep/1.1 depends on:
      |		DoesNotExist
      |""".stripMargin
    //new File("/tmp/actual").write(actual)
    //new File("/tmp/expected").write(expected)
    assertEndsWith(expected, actual)

/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    assertExists(file1dest)
    assertNotExists(file2dest)
    assertExists(file3dest)
    assertEquals(file1src.md5, file1dest.md5)
    assertEquals(file3src.md5, file3dest.md5)
                        
    printStats()
  }
}
