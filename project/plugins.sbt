scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.14")

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.277"

// git plugin
resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")

