scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint:_,-unused")

// jdeb and spotify docker are 'provided' in sbt-native-packager
libraryDependencies += "org.vafer" % "jdeb" % "1.9" artifacts (Artifact("jdeb", "jar", "jar"))
libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.12.5"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.1")

