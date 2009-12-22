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

class Upgrade_ContentMigration extends FunctionalTestCase {
  // the URL for the server to test against
  val serverLink = new URL(Tests.bazaarUrl)
  val universe = Tests.bazaarUniverse
  val testName = "Upgrade_ContentMigration"

  def testUpgrade {
    start()
/*============================================================================*\
**                           Prepare the test case                            **
\*============================================================================*/
    val srcDir:Filename = directory(packageBuildDir ::: "src1" :: Nil)
    val srcDir2:Filename = directory(packageBuildDir ::: "src2" :: Nil)
    
    // file1 is a simple text file for package 1
    val file1name = relfile("misc", testName, "file1.txt")
    val file1asrc = file1name.relativeTo(srcDir)
    val file1bsrc = file1name.relativeTo(srcDir2)

    // file2a is a simple text file for package 1 to be upgraded
    val file2aname = relfile("misc", testName, "file2.txt")
    val file2asrc = file2aname.relativeTo(srcDir)

    // file2b is a simple text file for package 1 after the upgrade
    val file2bname = relfile("misc", testName, "file2.txt")
    val file2bsrc = file2bname.relativeTo(srcDir2)

    // file3 is a simple text file moved between the packages on upgrade
    val file3name = relfile("misc", testName, "file3.txt")
    val file3src = file3name.relativeTo(srcDir)
    
    // file4 is a simple text file for package 2
    val file4name = relfile("misc", testName, "file4.txt")
    val file4src = file4name.relativeTo(srcDir)

    // file5 is a simple text file for package 2 removed by upgrade
    val file5name = relfile("misc", testName, "file5.txt")
    val file5src = file5name.relativeTo(srcDir)

    // file6 is a simple text file for package 2 added by upgrade
    val file6name = relfile("misc", testName, "file6.txt")
    val file6src = file6name.relativeTo(srcDir)

    // The packages containing the files
    val sbp1a = new File(packageBuildDir, testName + "_1-0.9.sbp")
    val sbp1b = new File(packageBuildDir, testName + "_1-1.0.sbp")
    val sbp2a = new File(packageBuildDir, testName + "_2-2.3.1.sbp")
    val sbp2b = new File(packageBuildDir, testName + "_2-2.3.2.sbp")

    val pack1a = new Package(
        testName + "_1",
        new Version("0.9"),
        ListSet.empty,
        "Package 1.0 for sbaz.functional." + testName + " test case")

    val pack1b = new Package(
        testName + "_1",
        new Version("1.0"),
        ListSet.empty,
        "Package 1.0 for sbaz.functional." + testName + " test case")

    val pack2a = new Package(
        testName + "_2",
        new Version("2.3.1"),
        ListSet.empty,
        "Package 1.1 for sbaz.functional." + testName + " test case")

    val pack2b = new Package(
        testName + "_2",
        new Version("2.3.2"),
        ListSet.empty,
        "Package 1.1 for sbaz.functional." + testName + " test case")

    // Make the files only if needed
    if(initDir(packageBuildDir)) {
      file1asrc.parent.mkdirs
      file1asrc.append("This is file #1 in package 1.")
      file1bsrc.parent.mkdirs
      file1asrc.copy(file1bsrc)

      file2asrc.parent.mkdirs
      file2asrc.append("This is file #2 in package 1 before upgrade")

      file2bsrc.parent.mkdirs
      file2bsrc.append("This is file #2 in package 1 after upgrade")

      file3src.parent.mkdirs
      file3src.append("This file should be in packages 1 before upgade and " +
        "package 2 after the upgrade.")

      file4src.parent.mkdirs
      file4src.append("This is file #4 in pagckage 2")

      file5src.parent.mkdirs
      file5src.append("This is file #5 in package 2 to be removed during upgrade")

      file6src.parent.mkdirs
      file6src.append("This is file #6 in package 2 added during upgrade")

      Zip.create(sbp1a, srcDir, file1name :: file2aname :: file3name :: Nil)
      Zip.create(sbp1b, srcDir2, file1name :: file2bname :: Nil)
      Zip.create(sbp2a, srcDir, file4name :: file5name :: Nil) 
      Zip.create(sbp2b, srcDir, file4name :: file3name :: file6name :: Nil)
    }
    setupDone()

/*============================================================================*\
**                       Publish the package to bazaar                        **
\*============================================================================*/
    {
      val availablePack1 = new AvailablePackage(pack1a, sbp1a.toURL());
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertTrue(res1 == OK())
      val availablePack2 = new AvailablePackage(pack2a, sbp2a.toURL());
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

    // Install package1
    val ret2: scala.tools.nsc.io.Process = execSbaz("install " + testName + "_1")
    assertEquals(0, ret2.waitFor)
    var downloads = 0
    ret2.foreach { 
      x => if (x contains "Downloading:") downloads = downloads + 1
      //println(x)
    }
    assertEquals(1, downloads)

    // Install package2
    val ret2b: scala.tools.nsc.io.Process = execSbaz("install " + testName + "_2")
    assertEquals(0, ret2b.waitFor)
    downloads = 0
    ret2b.foreach { 
      x => if (x contains "Downloading:") downloads = downloads + 1
      //println(x)
    }
    assertEquals(1, downloads)

    // Upgrade should result in a no-op 
    val ret3: scala.tools.nsc.io.Process = execSbaz("upgrade")
    assertEquals(0, ret3.waitFor)
    downloads = 0
    ret3.foreach { 
      x => if (x contains "Downloading:") downloads = downloads + 1
      //println(x)
    }
    assertEquals(0, downloads)
    
/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    val file1dest: File = file1name.relativeTo(managedDir)
    val file2adest: File = file2aname.relativeTo(managedDir)
    val file2bdest: File = file2aname.relativeTo(managedDir)
    val file3dest: File = file3name.relativeTo(managedDir)
    val file4dest: File = file4name.relativeTo(managedDir)
    val file5dest: File = file5name.relativeTo(managedDir)
    val file6dest: File = file6name.relativeTo(managedDir)
    
    assertTrue(file1dest.exists)
    assertTrue(file2adest.exists)
    assertTrue(file3dest.exists)
    assertTrue(file4dest.exists)
    assertTrue(file5dest.exists)
    assertFalse(file6dest.exists)
    assertEquals(file1asrc.md5, file1dest.md5)
    assertEquals(file2asrc.md5, file2adest.md5)
    assertFalse(file2bsrc.md5 equals file2bdest.md5)
    assertEquals(file3src.md5, file3dest.md5)
    assertEquals(file4src.md5, file4dest.md5)
    assertEquals(file5src.md5, file5dest.md5)
    
/*============================================================================*\
**                   Publish the package upgrade to bazaar                    **
\*============================================================================*/
    {
      val availablePack1 = new AvailablePackage(pack1b, sbp1b.toURL());
      val res1 = universe.requestFromServer(AddPackage(availablePack1))
      assertTrue(res1 == OK())
      val availablePack2 = new AvailablePackage(pack2b, sbp2b.toURL());
      val res2 = universe.requestFromServer(AddPackage(availablePack2))
      assertTrue(res2 == OK())
    }

    // Upgrade should result in a no-op 
    //execSbaz("update").foreach (x => println(x))
    //execSbaz("available").foreach (x => println(x))
    val ret4: scala.tools.nsc.io.Process = execSbaz("upgrade")
    assertEquals(0, ret4.waitFor)
    downloads = 0
    ret4.foreach { 
      x => if (x contains "Downloading:") downloads = downloads + 1
      //println(x)
    }
    assertEquals(2, downloads)

/*============================================================================*\
**                     Validate results in Managed Directory                  **
\*============================================================================*/
    assertTrue(file1dest.exists)
    assertTrue(file2bdest.exists)
    assertTrue(file3dest.exists)
    assertTrue(file4dest.exists)
    assertFalse(file5dest.exists)
    assertTrue(file6dest.exists)
    assertEquals(file1asrc.md5, file1dest.md5)
    assertFalse(file2asrc.md5 equals file2adest.md5)
    assertEquals(file2bsrc.md5, file2bdest.md5)
    assertEquals(file3src.md5, file3dest.md5)
    assertEquals(file4src.md5, file4dest.md5)
    assertEquals(file6src.md5, file6dest.md5)

    printStats()
  }
}
