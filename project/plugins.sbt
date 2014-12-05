addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-s3" % "0.8")

// git plugin
resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")
