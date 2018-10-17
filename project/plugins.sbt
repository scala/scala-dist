scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.6")

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.430"

// git plugin
resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")

