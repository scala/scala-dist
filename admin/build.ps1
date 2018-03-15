$ErrorActionPreference = "Stop"

Function checkExit() {
  if (-not $?) {
    echo "Last command failed."
    Exit 1
  }
}

Function ensureVersion() {
  $verPat="^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?$"
  if ($env:version -notmatch $verPat) {
    echo "Not a valid Scala version: '$env:version'"
    Exit 1
  }
}

Function clearIvyCache() {
  if (test-path C:\Users\appveyor\.ivy2\cache\org.scala-lang) {
    remove-item -force -recurse C:\Users\appveyor\.ivy2\cache\org.scala-lang
  }
  if (test-path C:\Users\appveyor\.ivy2) {
    get-childitem -path C:\Users\appveyor\.ivy2 -recurse -include "*compiler-interface*$env:version*" | remove-item -force -recurse
  }
  if (test-path C:\Users\appveyor\.sbt) {
    get-childitem -path C:\Users\appveyor\.sbt -recurse -include "*compiler-interface*$env:version*" | remove-item -force -recurse
  }
}

# oh boy. i don't (want to) fully understand, but executing commands and redirecting is difficult.
#  - https://mnaoumov.wordpress.com/2015/01/11/execution-of-external-commands-in-powershell-done-right/
#  - https://stackoverflow.com/a/35980675/248998
# letting cmd do the redirect avoids confusing powershell.
& cmd /c 'java -version' '2>&1'
checkExit

if ($env:APPVEYOR_FORCED_BUILD -eq 'true') {
  ensureVersion
  clearIvyCache
  if ($env:mode -eq 'release') {
    echo "Running a release for $env:version"
    $repositoriesFile="$env:APPVEYOR_BUILD_FOLDER\conf\repositories"
    & cmd /c "sbt ""-Dsbt.override.build.repos=true"" ""-Dsbt.repository.config=$repositoriesFile"" ""-Dproject.version=$env:version"" ""show fullResolvers"" clean update s3Upload" '2>&1'
    checkExit
  } else {
    echo "Unknown mode: '$env:mode'"
    Exit 1
  }
} else {
  $env:version="2.12.4"
  clearIvyCache
  # By default, test building the packages (but don't uplaod)
  # Need to redirect stderr, otherwise any error output (like jvm warning) fails the build (ErrorActionPreference)
  & cmd /c "sbt ""-Dproject.version=$env:version"" ""show s3Upload::mappings""" '2>&1'
  checkExit
}
