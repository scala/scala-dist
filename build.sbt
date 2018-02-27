import com.typesafe.sbt.SbtGit._
import ScalaDist.upload

// so we don't require a native git install
useJGit

// The version of this build determines the Scala version to package.
// We look at the closest git tag that matches v[0-9].* to derive it.
// For testing, the version may be overridden with -Dproject.version=...
versionWithGit

Versioning.settings

// necessary since sbt 0.13.12 for some dark and mysterious reason
// perhaps related to sbt/sbt#2634. details, to the extent they
// are known/understood, at scala/scala-dist#171
scalaVersion := version.value

mappings in upload := Seq()

upload := {
  import com.amazonaws.{ClientConfiguration, Protocol}
  import com.amazonaws.auth.{BasicAWSCredentials, AWSStaticCredentialsProvider, DefaultAWSCredentialsProviderChain}
  import com.amazonaws.services.s3.AmazonS3ClientBuilder
  import com.amazonaws.services.s3.model.PutObjectRequest
  import com.amazonaws.regions.Regions

  def env(v: String) = {
    val r = System.getenv(v)
    assert(r != null, s"Credentials env not specified: $v")
    r
  }

  val awsCreds = new BasicAWSCredentials(env("AWS_ACCESS_KEY_ID"), env("AWS_SECRET_ACCESS_KEY"))
  val client = AmazonS3ClientBuilder.standard
    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    .withRegion(Regions.US_EAST_1)
    .build

  val log = streams.value.log

  (mappings in upload).value map { case (file, key) =>
    log.info("Uploading "+ file.getAbsolutePath() +" as "+ key)
    client.putObject(new PutObjectRequest("downloads.typesafe.com", key, file))
  }
}

ScalaDist.settings

Docs.settings

ScalaDist.platformSettings

enablePlugins(UniversalPlugin, RpmPlugin, JDebPackaging, WindowsPlugin)

// resolvers += "local" at "file:///e:/.m2/repository"
// resolvers += Resolver.mavenLocal
// to test, run e.g., stage, or windows:packageBin, show s3-upload::mappings
