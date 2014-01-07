/*
      host in upload := "downloads.typesafe.com",
      mappings in upload <<= (packageZipTarball in Universal, packageBin in Universal, name, version) map
        {(tgz,zip,n,v) => Seq(tgz,zip) map {f=>(f,n+"/"+v+"/"+f.getName)}},
      progress in upload := true,
      credentials += Credentials(Path.userHome / ".s3credentials")
  settings(s3Settings:_*)
  settings(
    mappings in upload <<= (distributionFiles in OsConfig, scalaDistVersion) map makeDistFileMappings,
    downloadHtmlKey <<= (distributionFiles in OsConfig, target, scalaDistVersion) map { (dfiles, t, v) =>
      // TODO - reuse this value..
      val fileMap = makeDistFileMappings(dfiles, v)
      val links = fileMap map (_._2) map { name => """<li><a href="http://downloads.typesafe.com/%s">%s</a></li>""" format (name, name) }
      val html = """|<html>
                    |  <head><title>Scala Release %s files</title></head>
                    |  <body>
                    |     <h1> Scala Release %s files</h1>
                    |     <ul>
                    |       %s
                    |     <ul>
                    |  </body>
                    |</html>""".stripMargin format (v, v, links mkString "\n      ")
      val indexFile = t / "downloads.html"
      IO.write(indexFile, html)
      indexFile
    },
    mappings in upload <+= (downloadHtmlKey, scalaDistVersion) map { (html, v) => html -> ("scala/%s/index%s.html" format (v, if(isWindows) "-windows" else "")) },
    host in upload := "downloads.typesafe.com.s3.amazonaws.com",
    credentials += Credentials(Path.userHome / ".s3credentials")

*/