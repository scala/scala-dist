#!/bin/bash

set -ex

# Triggering jobs for a release:
#  - Open https://travis-ci.org/scala/scala-dist
#  - On the right: "More options" - "Trigger build"
#  - Chose the branch and enter a title for the build in the commit message filed
#  - Add a `before_install` custom config (see below) to set the `mode` and `version` env vars.
#    Using an `env: global: ...` section does not work because that overrides the default `env`
#    from .travis.yml. There's no way to specify the "merge mode" (*) from the web UI, that only
#    works in the REST API. We use `before_install` and assume it's not used otherwise.
#    (*) https://docs.travis-ci.com/user/triggering-builds/#Customizing-the-build-configuration
#  - Available modes:
#    - `release` to build native packages and upload them to S3
#    - `archive` to copy archives to chara (for scala-lang.org)
#    - `update-api` to update the scaladoc api symlinks
#    In all of the above modes, the `version` needs to be specified.
#
# before_install: export version=2.12.N mode=release


# Encryping files (if you need to encrypt a new file but no longer have the secret, create a new
# secret and re-encrypt all files):
#
# 1. Generate secret
#    cat /dev/urandom | head -c 10000 | openssl sha1 > ./secret
# 2. Save the secret on travis
#    travis encrypt "PRIV_KEY_SECRET=$(cat ./secret)"
# 3. Encrypt the file
#    openssl aes-256-cbc -pass "file:./secret" -in jenkins_lightbend_chara -out jenkins_lightbend_chara.enc
# 4. Decrypt the file
#    openssl aes-256-cbc -d -pass "pass:$PRIV_KEY_SECRET" -in admin/files/jenkins_lightbend_chara.enc > ~/.ssh/jenkins_lightbend_chara 2>/dev/null


function ensureVersion() {
  local verPat="[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?"
  [[ "$version" =~ $verPat ]] || {
    echo "Not a valid Scala version: '$version'"
    exit 1
  }
}

function decrypt() {
  # Even though we're running bash with -x, travis hides the private key from the log
  openssl aes-256-cbc -d -pass "pass:$PRIV_KEY_SECRET" -in $1 > $2 2>/dev/null
}

function setupSSH() {
  mkdir -p ~/.ssh
  cp admin/files/ssh_config ~/.ssh/config
  echo 'chara.epfl.ch,128.178.154.107 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBLER9rqy0iCfz24+8BBBObh7FXXQJCoLLE9UuyQHNFUU4SS5FSzNjEoKXwTj8nqNy+8l0rOkj3KG8p2cLxsqjY=' >> ~/.ssh/known_hosts
  decrypt admin/files/jenkins_lightbend_chara.enc ~/.ssh/jenkins_lightbend_chara
  chmod 700 ~/.ssh && chmod 600 ~/.ssh/*
}

curlOut="curlOut.txt"

function checkStatus() {
  cat $curlOut
  rm -f $curlOut
  [[ "$1" == "$2" ]] || {
    echo "Failed to start smoketest job"
    exit 1
  }
}

function triggerMsiRelease() {
  local jsonTemplate='{ "accountName": "scala", "projectSlug": "scala-dist", "branch": "%s", "commitId": "%s", "environmentVariables": { "mode": "%s", "version": "%s" } }'
  local json=$(printf "$jsonTemplate" "$TRAVIS_BRANCH" "$TRAVIS_COMMIT" "$mode" "$version")

  local curlStatus=$(curl \
    -s -o $curlOut -w "%{http_code}" \
    -H "Authorization: Bearer $APPVEYOR_TOKEN" \
    -H "Content-Type: application/json" \
    -d "$json" \
    https://ci.appveyor.com/api/builds)

  checkStatus $curlStatus "200"
}

function triggerSmoketest() {
  local jsonTemplate='{ "request": { "branch": "%s", "message": "Smoketest %s", "config": { "before_install": "export version=%s" } } }'
  local json=$(printf "$jsonTemplate" "$TRAVIS_BRANCH" "$version" "$version")

  local curlStatus=$(curl \
    -s -o $curlOut -w "%{http_code}" \
    -H "Travis-API-Version: 3" \
    -H "Authorization: token $TRAVIS_TOKEN" \
    -H "Content-Type: application/json" \
    -d "$json" \
    https://api.travis-ci.org/repo/scala%2Fscala-dist-smoketest/requests)

  checkStatus $curlStatus "202"
}

if [[ "$TRAVIS_EVENT_TYPE" == "api" ]]; then
  ensureVersion
  if [[ "$mode" == "archives" ]]; then
    echo "Running 'archives' for $version"
    setupSSH
    . scripts/jobs/release/website/archives
  elif [[ "$mode" == "update-api" ]]; then
    echo "Running 'update-api' for $version"
    setupSSH
    . scripts/jobs/release/website/update-api
  elif [[ "$mode" == "release" ]]; then
    echo "Running a release for $version"
    triggerMsiRelease
    repositoriesFile="$TRAVIS_BUILD_DIR/conf/repositories"
    # The log is too long for the travis UI, so remove ANSI codes to have a clean raw version
    sbt -Dsbt.log.noformat=true \
      -Dsbt.override.build.repos=true -Dsbt.repository.config="$repositoriesFile" \
      -Dproject.version=$version \
      "show fullResolvers" clean update s3Upload
    triggerSmoketest
  else
    echo "Unknown build mode: '$mode'"
    exit 1
  fi
else
  # By default, test building the packages (but don't uplaod)
  sbt -Dproject.version=2.12.4 "show s3Upload::mappings"
fi
