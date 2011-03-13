import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val izPackPlugin = "org.clapper" % "sbt-izpack-plugin" % "0.3.1"
  // val proguard = "org.scala-tools.sbt" % "sbt-proguard-plugin" % "0.0.5"
}
