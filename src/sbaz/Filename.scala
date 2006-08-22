/* SBAZ -- the Scala Bazaar
 * Copyright 2005-2006 LAMP/EPFL
 * @author  Lex Spoon
 */
// $Id$

package sbaz

import java.io.File
import scala.xml._

/** An abstract filename, portable to different platforms. */
class Filename(val isFile: Boolean,
               val isAbsolute: Boolean,
               val pathComponents: List[String])
extends Ordered[Filename]
{
  def isDirectory = !isFile

  def parent =
    new Filename(false, isAbsolute, pathComponents.reverse.tail.reverse)

  def relativeTo(filename: Filename): Filename = {
    if(isAbsolute)
      this
    else if(filename.isFile)
      relativeTo(filename.parent)
    else
      new Filename(isFile, filename.isAbsolute, filename.pathComponents ::: pathComponents)
  }

  def relativeTo(file: File): File = {
    pathComponents.foldLeft(file)((f, c) => new File(f, c))
  }

  override def toString: String = {
    val prefix = if(isAbsolute) "/" else ""
    val pathPart = pathComponents match {
      case Nil => ""
      case fst::rest => rest.foldLeft(fst)((path, comp) => path + "/" + comp)
    }
    val suffix = if(isFile) "" else " (dir)"
    prefix + pathPart + suffix
  }

  def toXML: Node =
<filename isFile={if(isFile) "true" else "false"} isAbsolute={if(isAbsolute) "true" else "false"}>
  {pathComponents.map(p => <pathcomp>{p}</pathcomp>)}
</filename>;

  override def compare(that: Filename): int = {
    def lexicomp(p1: List[String], p2: List[String]): int = {
      Pair(p1, p2) match {
        case Pair(Nil, Nil) => {
          // the paths are the same
          if(this.isAbsolute & !that.isAbsolute)  -1
          else if(!this.isAbsolute & that.isAbsolute)  1
          else if(this.isDirectory & !that.isDirectory)  -1
          else if(!this.isDirectory & that.isDirectory)  1
          else  0
        }
        case Pair(Nil, _) => -1
        case Pair(_, Nil) => 1
        case Pair(h1::t1, h2::t2) if h1<h2 => -1
        case Pair(h1::t1, h2::t2) if h1>h2 => 1
        case Pair(h1::t1, h2::t2) if h1==h2 => lexicomp(t1, t2)
      }
    }
    lexicomp(this.pathComponents, that.pathComponents)
  }

  override def equals(that: Any): Boolean = {
    that match {
      case that: Filename => this.compareTo(that) == 0
      case _ => false
    }
  }
}


object Filename {
  def file(parts: String*): Filename = new Filename(true, true, parts.toList)
  def relfile(parts: String*): Filename = new Filename(true, false, parts.toList)
  def directory(parts: String*): Filename = new Filename(false, true, parts.toList)
  def reldirectory(parts: String*): Filename = new Filename(false, false, parts.toList)

  def fromXML(xml: Node): Filename = {
    xml match {
      case xml: Elem => {
        if(xml.attributes.elements.exists(m => m.key == "isAbsolute")) {
          // new format
          val isAbsolute = xml.attributes.getValue("isAbsolute") == "true"
          val isFile = xml.attributes.getValue("isFile") == "true"
          val parts = (xml \ "pathcomp").toList.map(p => p.text)
          new Filename(isFile, isAbsolute, parts)
        } else {
          // old format
          val parts = xml.text.split("[/\\\\]").toList.filter(p => p!="")
          if(xml.text.startsWith("/") | xml.text.startsWith("\\"))
            Filename.file(parts:_*)
          else  
            Filename.relfile(parts:_*)
        }
      }
      
      case _ => throw new FormatError()
    }
  }
}
