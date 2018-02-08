scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint")

// jdeb and spotify docker are 'provided' in sbt-native-packager
libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))
libraryDependencies += "com.spotify" % "docker-client" % "8.9.0"
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.3")

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.277"

// git plugin
resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")

