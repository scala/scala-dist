#!/bin/bash

set -ex

function ensureVersion() {
  local verPat="[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9-]+)?"
  [[ "$version" =~ $verPat ]] || {
    echo "Not a valid Scala version: '$version'"
    exit 1
  }
}

function setupSSH() {
  mkdir -p ~/.ssh
  cp admin/files/ssh_config ~/.ssh/config
  echo 'chara.epfl.ch,128.178.154.107 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBBLER9rqy0iCfz24+8BBBObh7FXXQJCoLLE9UuyQHNFUU4SS5FSzNjEoKXwTj8nqNy+8l0rOkj3KG8p2cLxsqjY=' >> ~/.ssh/known_hosts
  # GitHub masks secrets in the log, but not reliably for multi-line ones
  (set +x; printf '%s\n' "$CHARA_SSH_KEY" > ~/.ssh/jenkins_lightbend_chara)
  chmod 700 ~/.ssh && $(cd ~/.ssh && chmod 600 config known_hosts jenkins_lightbend_chara)
}

clearIvyCache() {
  rm -fv $HOME/.ivy2/exclude_classifiers $HOME/.ivy2/exclude_classifiers.lock
  rm -rfv $HOME/.ivy2/cache/org.scala-lang $HOME/.ivy2/cache/org.scala-lang.modules
  rm -rfv $HOME/.ivy2/local/org.scala-lang $HOME/.ivy2/local/org.scala-lang.modules
  if [ -d $HOME/.ivy2 ]; then find $HOME/.ivy2 -name "*compiler-interface*$version*" | xargs rm -rfv; fi
  if [ -d $HOME/.sbt ]; then find $HOME/.sbt -name "*compiler-interface*$version*" | xargs rm -rfv; fi
}

if [[ -n "$mode" ]]; then
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
    if [[ "$version" =~ -bin- || "$version" =~ -pre- ]]; then
      sbt -Dsbt.log.noformat=true -Dproject.version=$version clean update "show ghUpload/mappings"
    else
      echo "Running a release for $version"
      sbt -Dsbt.log.noformat=true -Dproject.version=$version clean update ghUpload
    fi
  else
    echo "Unknown build mode: '$mode'"
    exit 1
  fi
else
  version="2.13.18"
  clearIvyCache
  # By default, test building the packages (but don't upload)
  sbt -Dsbt.log.noformat=true -Dproject.version=$version "show ghUpload/mappings"
fi
