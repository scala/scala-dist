package scbaz;

// A specification of a package.  It includes sufficient
// information to designate a package from a universe, but
// it does not include all of the information in a Package
case class PackageSpec(name:String, version:Version) {
  override def toString() = name + " " + version;

  def < (spec:PackageSpec) : Boolean = {
    (name < spec.name) ||
       (name.equals(spec.name) &&
	  version < spec.version)
  }
  // XXX should mix in an entire comparable trait
}



object TestPackageSpec {
  def main(args:Array[String]):Unit = {
    val version1 = new Version("1.4") ;
    val version2 = new Version("1" + ".4") ;
    val name1 = "hello" ;
    val name2 = "he" + "llo";

    val spec1 = PackageSpec(name1, version1);
    val spec2 = PackageSpec(name2, version2);

    Console.print(spec1.toString() + " equals " + spec2 + "? ");
    Console.println(spec1.equals(spec2));
  }
}
