/* SBaz -- Scala Bazaar
 * Copyright 2005-2008 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id: $

package sbaz

import junit.framework._
import Assert._

class FilenameTest extends TestCase {

  def testBasics  {
    val homedir = Filename.directory("home", "sbaz")
    val loginrel = Filename.relfile(".login")
    val login = loginrel.relativeTo(homedir )
    
    assertTrue(login.pathComponents == List("home", "sbaz", ".login"))
    assertTrue(login.isFile)        
  }
  
  def testExport {
    val totest = List(
        Filename.directory("home", "sbaz"),
        Filename.file("home", "sbaz", ".login"),
        Filename.relfile(".login"),
        Filename.file("/", "/blah/")
        )
        
    for (f <- totest) {
      val exported = f.toXML
      val imported = Filename.fromXML(exported)
      assertTrue(f == imported)
    }
  }
  
  def testOldFormat {
    val oldExp = <filename>lib/foo.jar</filename>;
    val file = Filename.fromXML(oldExp)
    val correct = Filename.relfile("lib", "foo.jar")
    assertTrue(file == correct)
    
    val oldExp2 = <filename>/home/sbaz/lib/foo.jar</filename>;
    val file2 = Filename.fromXML(oldExp2)
    val correct2 = Filename.file("home", "sbaz", "lib", "foo.jar")
    assertTrue(file2 == correct2)
  }
}
