import ScalaDist.{s3Upload, ghUpload}

resolvers += "scala-integration" at "https://scala-ci.typesafe.com/artifactory/scala-integration/"

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

s3Upload / mappings := Seq()

s3Upload := {
  import com.amazonaws.services.s3.AmazonS3ClientBuilder
  import com.amazonaws.services.s3.model.PutObjectRequest
  import com.amazonaws.regions.Regions

  // The standard client picks credentials from AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY env vars
  val client = AmazonS3ClientBuilder.standard.withRegion(Regions.US_EAST_1).build

  val log = streams.value.log
    (s3Upload / mappings).value map { case (file, key) =>
    log.info("Uploading "+ file.getAbsolutePath() +" as "+ key)
    client.putObject(new PutObjectRequest("downloads.typesafe.com", key, file))
  }
}

ghUpload := {
  import sttp.client3._
  import _root_.io.circe._, _root_.io.circe.parser._

  val log = streams.value.log
  val ghRelease = s"v${(Universal / version).value}"

  val token = sys.env.getOrElse("GITHUB_OAUTH_TOKEN", throw new MessageOnlyException("GITHUB_OAUTH_TOKEN missing"))

  val backend = HttpURLConnectionBackend()

  val rRes = basicRequest
    .get(uri"https://api.github.com/repos/scala/scala/releases/tags/$ghRelease")
    .header("Accept", "application/vnd.github+json")
    .header("Authorization", s"Bearer $token")
    .header("X-GitHub-Api-Version", "2022-11-28")
    .send(backend)
  val releaseId = rRes.body.flatMap(parse).getOrElse(Json.Null).hcursor.downField("id").as[Int].getOrElse(
    throw new MessageOnlyException(s"Release not found: $ghRelease"))

  (s3Upload / mappings).value map { case (file, _) =>
    log.info(s"Uploading ${file.getAbsolutePath} as ${file.getName} to https://github.com/scala/scala/releases/tag/$ghRelease")

    // https://docs.github.com/en/rest/releases/assets?apiVersion=2022-11-28#upload-a-release-asset
    val request = basicRequest
      .post(uri"https://uploads.github.com/repos/scala/scala/releases/${releaseId}/assets?name=${file.getName}")
      .contentType("application/octet-stream")
      .header("Accept", "application/vnd.github+json")
      .header("Authorization", s"Bearer $token")
      .header("X-GitHub-Api-Version", "2022-11-28")
      .body(file)

    val response = request.send(backend)
    if (response.code.code != 201)
      throw new MessageOnlyException(s"Upload failed: status=${response.code}\n${response.body}")
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
