scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint:_,-unused")

// jdeb and spotify docker are 'provided' in sbt-native-packager
libraryDependencies += "org.vafer" % "jdeb" % "1.14" artifacts (Artifact("jdeb", "jar", "jar"))
libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.13.0")

libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.11.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.14.17")

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.2.0")
