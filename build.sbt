import com.typesafe.sbt.SbtGit._

// so we don't require a native git install
useJGit

// The version of this build determines the Scala version to package.
// We look at the closest git tag that matches v[0-9].* to derive it.
// For testing, the version may be overridden with -Dproject.version=...
versionWithGit

Generic.settings

Docs.settings

Wix.settings

Unix.settings

// for local testing (on windows)
// resolvers += Resolver.mavenLocal
// resolvers += "local" at "file:///e:/.m2/repository"
// to test, run e.g., windows:packageBin