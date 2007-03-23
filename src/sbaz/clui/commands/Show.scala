/* SBaz -- Scala Bazaar
 * Copyright 2005-2007 LAMP/EPFL
 * @author  Lex Spoon
 */

// $Id$

package sbaz.clui.commands

object Show extends Command {
  val name = "show"
  val oneLineHelp = "show information about one package"
  val fullHelp: String = (
    "show package\n" +
    "show -f file\n" +
    "\n" +
    "Show a summary of the specified package.  In the first form, the\n" +
    "package can be specified as either \"name\" or \"name/version\", where,\n" +
    "if no version is specified, the newest available version is\n" +
    "displayed.  If there is a package already installed with the requested\n" +
    "specification, then that package is shown in preference to a matching\n" +
    "package from the bazaar.\n" +
    "\n" +
    "In the second form, display the contents of the specified package file.\n")

  def run(args: List[String], settings: Settings) = {
    import settings._

    args match {
      case List(spec) =>
        val uspec = UserPackageSpecifierUtil.fromString(spec)
      
        uspec.chooseFrom(dir.installed) match {
          case Some(pack) =>
            Console.println(pack.pack.longDescription)
            Console.println("Files included:")
            for (val file <-pack.files)
              Console.println("  " + file)

          case None =>
            uspec.chooseFrom(dir.available) match {
              case Some(pack) =>
                Console.println(pack.pack.longDescription)
                Console.println("Link: " + pack.link)
              
              case None =>
                Console.println("No available package matches " + spec)
            }
        }

      case List("-f", filename) =>
        val packfile = new PackageFile(filename)
        val pack = packfile.pack
        Console.println(pack.longDescription)
        Console.println("Files included:")
        for (val fn <- packfile.fileNames)
          Console.println("  " + fn)
      
      case _ =>
        usageExit
    }
  }
}
