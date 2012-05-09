resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe" % "sbt-native-packager" % "0.4.2")

libraryDependencies += "net.databinder" %% "dispatch-http" % "0.8.6"
