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
  remove-item -erroraction ignore -force -verbose $homeDir\.ivy2\exclude_classifiers, $homeDir\.ivy2\exclude_classifiers.lock
  remove-item -erroraction ignore -force -verbose -recurse $homeDir\.ivy2\cache\org.scala-lang, $homeDir\.ivy2\cache\org.scala-lang.modules
  remove-item -erroraction ignore -force -verbose -recurse $homeDir\.ivy2\local\org.scala-lang, $homeDir\.ivy2\local\org.scala-lang.modules
  get-childitem -erroraction ignore -path $homeDir\.ivy2 -recurse -include "*compiler-interface*$env:version*" | remove-item -force -recurse -verbose
  get-childitem -erroraction ignore -path $homeDir\.sbt -recurse -include "*compiler-interface*$env:version*" | remove-item -force -recurse -verbose
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
  $env:version="2.13.6"
  clearIvyCache
  # By default, test building the packages (but don't uplaod)
  # Need to redirect stderr, otherwise any error output (like jvm warning) fails the build (ErrorActionPreference)
  & cmd /c "sbt ""-Dproject.version=$env:version"" ""show s3Upload/mappings""" '2>&1'
  checkExit
}
