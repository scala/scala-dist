/* SBaz -- Scala Bazaar
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

import java.io.{File, FileReader, FileInputStream,
                FileOutputStream, OutputStream, FileWriter,
                IOException}
import java.net.URL
import java.util.zip.{ZipOutputStream, ZipEntry}

import sbaz._
import sbaz.FileUtils._
import sbaz.clui._
import scala.collection.immutable.ListSet

object Pack extends Command {
  val name = "pack"
    val oneLineHelp = "create an sbaz package"
    val fullHelp: String = (
      "pack name directory [options]\n" +
      "\n" +
      "Options (defaults):\n" +
      "  --version version (0.0)\n" +
      "  --description description (no description)\n" +
      "  --descfile description-file (no description)\n" +
      "  --depends dependencies seperated by a comma (none)\n" +
      "  --outdir directory (current directory)\n" +
      "  --linkbase url (none)\n" +
      "  --pack200\n"+
      "\n" +
      "Create an sbaz package and, if a link base is specified, an\n" +
      "advertisement file.  The package file is named name-version.sbp.\n" +
      "The advertisement file is named name-version.advert.  The URL\n" +
      "in the advertisement file is the URL base with the package\n" +
      "filename appended. If the pack200 flag is given, all jar files\n" +
      "will have the pack200 encoding applied.\n" +
      "\n" +
      "NOTE: The pack200 encoding can be resource intensive for large\n" +
      "packages.  If sbaz exits unexpectedly, you may need to allocate\n" +
      "more memory to the heap and/or permgen space.\n")

  abstract class Settings {
    val packdir: File              // directory to pack up
    val outdir: File               // directory to hold sbp and advert files

    val name: String               // name of the package
    val version: Version           // version of the package
    val description: String        // description of the package
    val depends: List[String]      // dependencies of the package

    val linkBase: Option[String]   // base of the link URL 
    val applyPack200: Boolean      // toggles use of pack200 for jar files
  }
  

  def parseArguments(args: List[String]): Settings = {
    var argsLeft: List[String] = args
    var nameArg: String = null
    var packdirArg: File = null
    var outdirArg: File = null
    var linkBaseArg: Option[String] = None
    var versionArg: Version = new Version("0.0")
    var descriptionArg: String = "(no description)"
    var dependsArg: List[String] = Nil
    var applyPack200Arg: Boolean = false

    args match {
      case name0 :: packdir0 :: rest =>
        nameArg = name0
        packdirArg = new File(packdir0)
        argsLeft = rest
        
      case _ =>
        usageExit
    }
    
    while(argsLeft != Nil) {
      argsLeft match {
        case "--version" :: ver :: rest =>
          versionArg = new Version(ver)
          argsLeft = rest
        
        case "--description" :: desc :: rest =>
          descriptionArg = desc
          argsLeft = rest

        case "--descfile" :: df :: rest =>
          val str = try {
            descriptionArg = readFile(df)
          } catch {
            case er:IOException =>
              Console.println("Error reading " + df + ": " + er)
              exit(1)
          }
              
          argsLeft = rest

        case "--depends" :: dep :: rest =>
          dependsArg = dep.split(",").toList
          argsLeft = rest

        case "--outdir" :: outd :: rest =>
          outdirArg = new File(outd)
          argsLeft = rest
          
        case "--linkbase" :: lb :: rest =>
          linkBaseArg = Some(lb)
          argsLeft = rest

        case "--pack200" :: rest =>
          applyPack200Arg = true
          argsLeft = rest
          
        case _ => usageExit
      }
    }
    
    new Settings {
      val name = nameArg
      val packdir = packdirArg
      val version = versionArg
      val description = descriptionArg
      val linkBase = linkBaseArg
      val outdir = outdirArg
      val depends = dependsArg
      val applyPack200 = applyPack200Arg
    }
  }

  /** Traverse a directory tree in pre-order.  The arguments
    * to the handler are the Java File object and the
    * path relative to the root, using forward slash as
    * the delimiter.
    */
  def withDirTree(root: File)(handler: (File,String) => Unit) {
    def lp(file: File, path: String): Unit = {
      handler(file, path)
      if(file.isDirectory) {
        for (entry <- file.listFiles.toList)
          lp(entry, path + "/" + entry.getName)
      }
    }
    lp(root, "")
  }

  /** Create the package entry for a specified Settings */
  def packageFor(settings: Settings) =
    new Package(settings.name, 
                settings.version, 
                ListSet.empty[String] ++ settings.depends, 
                settings.description) 

  /** Write an SBP file.  Throws IOException's if anything goes wrong. */
  def writeSBP(sbazSettings: sbaz.clui.Settings, packSettings: Settings) {
    import sbazSettings.verbose
    
    val sbpName = packSettings.name + "-" + packSettings.version + ".sbp"
    val sbpFile = new File(packSettings.outdir, sbpName)
    if(verbose)
      Console.println("Writing " + sbpFile + "...")
    val zip = new ZipOutputStream(new FileOutputStream(sbpFile))

    withDirTree(packSettings.packdir)((file, zippath) => {
      if (!file.isDirectory) {
        if (packSettings.applyPack200 && isJar(zippath)) {
          val packzippath = rename(zippath, ".jar", ".pack")
          if (verbose) 
            println("Packing " + zippath + " to " + packzippath + "...")
          val packFile = renameFile(file, ".jar", ".pack")
          if (packFile.exists) { 
            throw new Error("A pack200 file could not be generated because " +
              "destination file already exists: \n\t" + packFile.toString ) 
          }
          try {
            pack200(file, packFile)
            if (verbose) println("Adding " + packzippath + "...")
            zip.putNextEntry(new ZipEntry(packzippath))
            copyFile(packFile, zip)
            zip.closeEntry
            packFile.delete
          } catch {
            // Make sure to clean up on error
            case t: Throwable => {
              packFile.delete
              throw t
            }
          }
        }
        else {
          if (verbose) println("Adding " + zippath + "...")
          zip.putNextEntry(new ZipEntry(zippath))
          copyFile(file, zip)
          zip.closeEntry
        }
      }
    })
    
    if (verbose)
      println("Writing meta/description")
          
    zip.putNextEntry(new ZipEntry("meta/description"))
    zip.write(packageFor(packSettings).toXML.toString.getBytes)
    zip.closeEntry
    
    zip.close
    if (verbose)
      println("Finished with " + sbpFile + ".")
  }
  
  def writeAdvert(sbazSettings: sbaz.clui.Settings, packSettings: Settings) {
    import sbazSettings.verbose
    
    val link =
      (packSettings.linkBase.get + 
       packSettings.name + "-" + 
       packSettings.version + ".sbp")
      
    val advertFile = 
      new File(packSettings.name + "-" + packSettings.version + ".advert")
      
    if(verbose)
      Console.println("writing " + advertFile + "...")
      
    val advert = new AvailablePackage(packageFor(packSettings), new URL(link))
    
    val out = new FileWriter(advertFile)
    out.write(advert.toXML.toString)
    out.close
  }
  
  def run(args: List[String], sbazSettings: sbaz.clui.Settings) {
    val packSettings = parseArguments(args)

    PackageUtil.checkName(packSettings.name) match {
      case Some(problem) => println("Warning: " + problem)
      case None => ()
    }

    VersionUtil.check(packSettings.version.toString) match {
      case Some(problem) => println("Warning: " + problem)
      case None => ()
    }

    writeSBP(sbazSettings, packSettings)
    if (!packSettings.linkBase.isEmpty)
      writeAdvert(sbazSettings, packSettings)
  }
}
