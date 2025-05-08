scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint:_,-unused")

// jdeb and spotify docker are 'provided' in sbt-native-packager
libraryDependencies += "org.vafer" % "jdeb" % "1.9" artifacts (Artifact("jdeb", "jar", "jar"))
libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.12.5"

libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.11.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.14.13")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.1")
