#!/bin/bash

set -ex

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
  chmod 700 ~/.ssh && $(cd ~/.ssh && chmod 600 config known_hosts jenkins_lightbend_chara)
}

function triggerMsiRelease() {
  local jsonTemplate='{ "accountName": "scala", "projectSlug": "scala-dist", "branch": "%s", "commitId": "%s", "environmentVariables": { "mode": "%s", "version": "%s", "scala_sha": "%s" } }'
  local json=$(printf "$jsonTemplate" "$TRAVIS_BRANCH" "$TRAVIS_COMMIT" "$mode" "$version" "$scala_sha")

  local curlStatus=$(curl \
    -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $APPVEYOR_TOKEN" \
    -H "Content-Type: application/json" \
    -d "$json" \
    https://ci.appveyor.com/api/builds)

  [[ "$curlStatus" == "200" ]] || {
    echo "Failed to start AppVeyor build"
    exit 1
  }
}

function triggerSmoketest() {
  local jsonTemplate='{ "request": { "branch": "%s", "message": "Smoketest %s", "config": { "before_install": "export version=%s scala_sha=%s" } } }'
  local json=$(printf "$jsonTemplate" "$TRAVIS_BRANCH" "$version" "$version" "$scala_sha")

  local curlStatus=$(curl \
    -s -o /dev/null -w "%{http_code}" \
    -H "Travis-API-Version: 3" \
    -H "Authorization: token $TRAVIS_TOKEN" \
    -H "Content-Type: application/json" \
    -d "$json" \
    https://api.travis-ci.org/repo/scala%2Fscala-dist-smoketest/requests)

  [[ "$curlStatus" == "202" ]] || {
    echo "Failed to start travis build"
    exit 1
  }
}

clearIvyCache() {
  rm -f $HOME/.ivy2/exclude_classifiers $HOME/.ivy2/exclude_classifiers.lock
  rm -rf $HOME/.ivy2/cache/org.scala-lang
  rm -rf $HOME/.ivy2/local/org.scala-lang
  if [ -d $HOME/.ivy2 ]; then find $HOME/.ivy2 -name "*compiler-interface*$version*" | xargs rm -rf; fi
  if [ -d $HOME/.sbt ]; then find $HOME/.sbt -name "*compiler-interface*$version*" | xargs rm -rf; fi
}

if [[ "$TRAVIS_EVENT_TYPE" == "api" ]]; then
  ensureVersion
  clearIvyCache
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
  version="2.12.4"
  clearIvyCache
  # By default, test building the packages (but don't uplaod)
  sbt -Dproject.version=$version "show s3Upload::mappings"
fi
