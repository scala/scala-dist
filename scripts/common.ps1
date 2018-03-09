Function postCommitStatus($state) {
  if ("$env:scala_sha" -ne "") {
    $jsonTemplate = '{{ "state": "{0}", "target_url": "{1}", "description": "{2}", "context": "{3}" }}'
    $json = "$jsonTemplate" -f "$state", "https://ci.appveyor.com/project/scala/scala-dist/build/$env:APPVEYOR_BUILD_ID", "$state", "appveyor/scala-dist/$env:version/$env:mode"

    # https://stackoverflow.com/questions/41618766/powershell-invoke-webrequest-fails-with-ssl-tls-secure-channel
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

    $respone = Invoke-WebRequest `
      "https://api.github.com/repos/scala/scala/statuses/$env:scala_sha" `
      -Method 'POST' `
      -Body "$json" `
      -Headers @{"Accept"="application/vnd.github.v3+json"; "Authorization"="token $env:GITHUB_OAUTH_TOKEN"} `
      -UseBasicParsing

    if ($respone.StatusCode -ne 201) {
      echo "Failed to publish GitHub commit status"
      Exit 1
    }
  }
}
