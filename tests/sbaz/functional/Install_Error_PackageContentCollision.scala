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

class Install_Error_PackageContentCollision extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Install_Error_PackageContentCollision"

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
    val file2TextName = relfile("lib", testName, "file2.txt")
    val file2TextSrc = file2TextName.relativeTo(srcDir)
    val file2ZipName = relfile("lib", testName, "file2.jar")
    val file2PackName = relfile("lib", testName, "file2.pack")
    val file2ZipSrc = file2ZipName.relativeTo(srcDir)

    val sbp1 = new File(packageBuildDir, testName + "1.sbp")
    val sbp2 = new File(packageBuildDir, testName + "2.sbp")

    val pack1 = new Package(
        testName + "_package1",
        new Version("1.0"),
        ListSet.empty,
        "Package 1 for sbaz.functional." + testName + " test case")

    val pack2 = new Package(
        testName + "_package2",
        new Version("0.5"),
        ListSet.empty,
        "Package 2 for sbaz.functional." + testName + " test case")

    // Make the files only if needed
    if(initDir(packageBuildDir)) {
      file1src.parent.mkdirs
      file1src.append("This is file #1")
      
      file2TextSrc.parent.mkdirs
      file2TextSrc.append("This is file #2")
      Zip.create(file2ZipSrc, srcDir, file2TextName :: Nil)
      val file2PackedSrc = file2ZipSrc.repack200.pack200

      // Jar up into the sbp package file
      Zip.create(sbp1, srcDir, file1name :: file2PackName :: Nil)
      assertExists(sbp1)
      sbp1.copy(sbp2)
      assertExists(sbp2)
    }
    setupDone()

/*============================================================================*\
**                       Publish the package to bazaar                        **
\*============================================================================*/
    {
      // Submit a dummy earlier version
      val availablePack1 = new AvailablePackage(pack1, sbp1.url)
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertEquals(OK(), res1)

      // Submit the actual package
      val availablePack2 = new AvailablePackage(pack2, sbp2.url)
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
    //ret1.foreach( x => println(x) )
    assertEquals(getOutput(ret1), 0, ret1.waitFor)
    val ret2: scala.tools.nsc.io.Process = execSbaz("install " + testName + "_package1")
    //ret2.foreach( x => println(x) )
    assertEquals(getOutput(ret2), 0, ret2.waitFor)
    val ret3: scala.tools.nsc.io.Process = execSbaz("install " + testName + "_package2")
    //ret2.foreach( x => println(x) )
    assertEquals(getOutput(ret3), 1, ret3.waitFor)

    val expected =
      """Error: Action aborted due to inter-package content collisions.
        |	Install_Error_PackageContentCollision_package2/0.5 collides with:
        |		Install_Error_PackageContentCollision_package1/1.0
        |""".stripMargin
    val actual = ret3.mkString("", "\n", "")
    //new File("/tmp/actual").write(actual)
    //new File("/tmp/expected").write(expected)
    assertEndsWith(expected, actual)
/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    val file1dest = file1name.relativeTo(managedDir)
    val file2ZipDest = file2ZipName.relativeTo(managedDir)
    assertEquals(file1src.md5, file1dest.md5)
    assertEquals(file2ZipSrc.md5, file2ZipDest.md5)
    printStats()
  }
}
