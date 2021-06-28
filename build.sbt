import ScalaDist.upload

// so we don't require a native git install
useJGit

// The version of this build determines the Scala version to package.
// We look at the closest git tag that matches v[0-9].* to derive it.
// For testing, the version may be overridden with -Dproject.version=...
versionWithGit

isSnapshot := {
  git.overrideVersion(git.versionProperty.value) match {
    case Some(v) => v.endsWith("-SNAPSHOT") || git.gitUncommittedChanges.value
    case _ => isSnapshot.value // defined in SbtGit.scala
  }
}

Versioning.settings

// necessary since sbt 0.13.12 for some dark and mysterious reason
// perhaps related to sbt/sbt#2634. details, to the extent they
// are known/understood, at scala/scala-dist#171
scalaVersion := version.value

upload / mappings := Seq()

upload := {
  import com.amazonaws.services.s3.AmazonS3ClientBuilder
  import com.amazonaws.services.s3.model.PutObjectRequest
  import com.amazonaws.regions.Regions

  // The standard client picks credentials from AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY env vars
  val client = AmazonS3ClientBuilder.standard.withRegion(Regions.US_EAST_1).build

  val log = streams.value.log
    (upload / mappings).value map { case (file, key) =>
    log.info("Uploading "+ file.getAbsolutePath() +" as "+ key)
    client.putObject(new PutObjectRequest("downloads.typesafe.com", key, file))
  }
}

ScalaDist.settings

Docs.settings

ScalaDist.platformSettings

enablePlugins(UniversalPlugin, RpmPlugin, JDebPackaging, WindowsPlugin)

// TODO This silences a warning I don't understand.
//
//  * scala-dist / Universal / configuration
//    +- /Users/jz/code/scala-dist/build.sbt:35
//  * scala-dist / Universal-docs / configuration
//    +- /Users/jz/code/scala-dist/build.sbt:35
//  * scala-dist / Universal-src / configuration
//    +- /Users/jz/code/scala-dist/build.sbt:35
Global / excludeLintKeys += configuration

// resolvers += "local" at "file:///e:/.m2/repository"
// resolvers += Resolver.mavenLocal
// to test, run e.g., stage, or windows:packageBin, show s3Upload::mappings
