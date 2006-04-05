package sbaz.clui.commands
import scala.collection.immutable._

object Available extends Command {
  val name = "available"
  val oneLineHelp = "list the available packages for installation"
  val fullHelp: String = (
    "available [ -a ]\n" +
    "\n" +
    "Display the list of packages that are available for installation.\n" +
    "If -a is specified, then print out all versions of each package instead\n" +
    "of just the most recent ones.")



  def run(args: List[String], settings: Settings) = {
    import settings._

    var printall = false
    
    args match {
      case Nil => ()
      case List("-a") => printall=true
      case _ => usageExit
    }

    val specs = dir.available.packages.toList.map(.spec)
		val nameSet = specs.foldLeft(new TreeSet[String])((set,spec) => set + spec.name)
    val names = nameSet.toList.sort((a,b) => a <= b)

    def versionsFor(name: String) = {
      val unsorted =
        for {
          val spec <- specs
          spec.name == name
        } yield spec.version
      unsorted.sort((v1,v2) => v1 >= v2)
    }
      
    def printVersions(specs: List[Version]) =
      Console.print(specs.mkString("", ", ", ""))
      
    def printAllSpecs(name: String) =
      printVersions(versionsFor(name))
    
    def printShortSpecs(name: String) = {
      val toprint = 3
      val matching = versionsFor(name)
      if(matching.length <= toprint)
        printVersions(matching)
      else {
        printVersions(matching.take(toprint))
        Console.print(", ...")
      }
    }


    for(val name <- names) {
      Console.print(name + " (")
      if(printall)
        printAllSpecs(name)
      else
        printShortSpecs(name)
      Console.println(")")
    }
    Console.println(names.length.toString + " package names")
    Console.println(specs.length.toString + " total packages")
  }
}
