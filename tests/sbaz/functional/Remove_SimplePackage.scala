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

class Remove_SimplePackage extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Remove_SimplePackage"

  def testRemove {
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

    val descFilename = relfile("meta", "description")
    val descriptionFile = descFilename.relativeTo(srcDir)

    val sbp = new File(packageBuildDir, testName + ".sbp")

    val pack = new Package(
        testName,
        new Version("1.0"),
        ListSet.empty,
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
      assertTrue(sbp.exists)
    }
    setupDone()

/*============================================================================*\
**                       Publish the package to bazaar                        **
\*============================================================================*/
    {
      // Submit a dummy earlier version
      val availablePack1 = new AvailablePackage(prevPack, 
          new URL("http://nowhere/file.zip"))
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertTrue(res1 == OK())

      // Submit the actual package
      val availablePack2 = new AvailablePackage(pack, sbp.url);
      val res2 = universe.requestFromServer(AddPackage(availablePack2))
      assertTrue(res2 == OK())
    }
    publishDone()

/*============================================================================*\
**                     Install package in Managed Directory                   **
\*============================================================================*/
    {
      val universeFile = file(srcDir ::: "universe" :: Nil)
      universeFile.write(Tests.bazaarUniverse.toXML.toString)
      val ret1: scala.tools.nsc.io.Process = execSbaz("setuniverse  \"" 
        + universeFile.relativeTo(new File("")) + "\"")
      //ret1.foreach( x => println(x) )
      assertEquals(0, ret1.waitFor)
      val ret2: scala.tools.nsc.io.Process = execSbaz("install " + testName)
      //ret2.foreach( x => println(x) )
      assertEquals(0, ret2.waitFor)
      val ret3: scala.tools.nsc.io.Process = execSbaz("installed")
      //assertEquals(0, ret3.waitFor)
      var hits = 0
      ret3.foreach {line => if (line.trim.equals(pack.spec.toString)) hits = hits + 1}
      assertEquals(1, hits)
    }
    
/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    val file1dest: File = file1name.relativeTo(managedDir)
    val file2ZipDest: File = file2ZipName.relativeTo(managedDir)
    assertEquals(file1src.md5, file1dest.md5)
    assertEquals(file2ZipSrc.md5, file2ZipDest.md5)
    
/*============================================================================*\
**                     Remove package in Managed Directory                    **
\*============================================================================*/
    {
      val ret1: scala.tools.nsc.io.Process = execSbaz("remove " + testName)
      assertEquals(0, ret1.waitFor)
      val ret2: scala.tools.nsc.io.Process = execSbaz("installed")
      assertEquals(0, ret2.waitFor)
      var hits = 0
      ret2.foreach(line => if (line.trim.equals(pack.spec.toString)) hits = hits + 1)
      assertEquals(0, hits)
    }
    
/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    assertFalse(file1dest.exists)
    assertFalse(file2ZipDest.exists)
    assertFalse(file1dest.getParentFile.exists)
    
    printStats()
  }
}
