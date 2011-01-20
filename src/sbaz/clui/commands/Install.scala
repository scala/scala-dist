/* SBaz -- Scala Bazaar
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Lex Spoon
 */


package sbaz.clui.commands

import java.io.File
import sbaz.ProposedChanges._
import sbaz._
import sbaz.clui._

object Install extends Command {
  val name = "install"
  val oneLineHelp = "install a package"
  val fullHelp =
    """install package
    |
    |Install a package, including any of its necessary dependencies.
    |The package to install is specified in one of the following ways:
    |
    |    name         - Install the newest package with the specified name
    |    name/version - Install a package with a specified name and version
    |    -f filename  - Install the package located in the specified file
    |""".stripMargin

  def run(args: List[String], settings: Settings) {
    import settings._

    var namedAdditions: Set[Addition] = Set.empty
    
    // Parse the args
    var argsLeft = args
    while (argsLeft != Nil) {
      argsLeft match {
        case "-f" :: filename :: rest => {
          val file = new File(filename)
          if(!file.exists) throw new Error("File '" + filename + "' does not exist.")
          val addition = AdditionFromFile(file)
          namedAdditions = namedAdditions + addition
          argsLeft = rest
        }
      
        case packageName :: rest => {
          val userSpec =
            try {
              UserPackageSpecifierUtil.fromString(packageName)
            } catch {
              case e: FormatError =>
                val isFile = new File(packageName).isFile()
                throw new Error ( if (isFile) "Use option '-f filename' to specify file names" else e.getMessage, e)
            }

          val addition = userSpec.chooseFrom(dir.available) match {
            case None =>
              throw new Error("No available package matches '" + packageName + "'!")
            case Some(pack) =>
              AdditionFromNet(pack)
          }
          namedAdditions = namedAdditions + addition
          argsLeft = rest
        }
        
        case _ => usageExit
      }
    }

    // Ensure each package is represented only once (not multiple versions)
    // TODO: If same package identified on command line twice, problems ensue.
    
    // Identify all new dependencies
    var postInstallPackageNames = dir.installed.packageNames ++ namedAdditions.map(_.pack.name).iterator
    val additions = namedAdditions.foldLeft[Set[Addition]](namedAdditions) { (dependencies, addition) => {
      val depPackages = dir.available.chooseDependencyPackagesFor(addition.pack, postInstallPackageNames)
      postInstallPackageNames = postInstallPackageNames ++ depPackages.map(_.pack.name).iterator
      dependencies ++ depPackages.map(p => AdditionFromNet(p)).reverse
    }}

    // Order the dependency installation properly
    // TODO: The above is kind of hack-ish and doesn't properly order when dependencies are
    // specified by command line.  Could use a sort by compare with dependencies first and
    // name second. Presently, this isn't too important because audits exist to prevent
    // packages from overlapping their contents.  If "patch" style packages are introduced to
    // overwrite other packages, this ordering will become much more important.

    for (addition <- additions)
      println("planning to install: " + addition.pack.spec)

    if (!dryrun) {
      println("Installing...")
      dir.makeChanges(additions.toSeq.reverse)
    }
  }
}
