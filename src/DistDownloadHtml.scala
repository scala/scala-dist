// TODO: integrate into jenkins/SBT job

// where to find the files
def root(version: String) = s"/Users/adriaan/scala-dists/$version"

// TODO: rpm & deb
// TODO: get source snapshot from github?
def fileMapping(version: String) = List(
  s"scala-$version.tgz" ->              "Unix, Mac OS X, Cygwin",
  s"scala-$version.zip" ->              "Windows (zip archive)",
  s"scala-$version.msi" ->              "Windows (msi)",
  s"scala-docs-$version.txz" ->         "Scala API (txz)",
  s"scala-docs-$version.zip" ->         "Scala API (zip)",
  s"scala-tool-support-$version.tgz" -> "Scala tool support (tgz)",
  s"scala-tool-support-$version.zip" -> "Scala tool support (zip)")


def size(file: String) = new java.io.File(file).length match {
   case s if s / (1024 * 1024) >= 1 => s"${s / (1024 * 1024)} MB"
   case s => s"${s / 1024} KB"
}

def api(version: String) = s"/api/$version"
def url(file: String) = s"/downloads/distrib/files/$file"

// TODO: md5/sha1
def downloads(version: String) = fileMapping(version).map { case (file, desc) =>
   <tr>
       <td>{desc}</td>
       <td><a style="text-decoration: none;" href={url(file)}>{file}</a></td>
       <td style="text-align: right;">{size(s"${root(version)}/$file")}</td>
   </tr>
}

def download(version: String) =
  <span><table width="75%" cellspacing="5" cellpadding="1" align="center">
      <tbody>{downloads(version)}</tbody>
  </table><p>You can also browse the Scala {version} <a href={api(version)}>API online</a>.</p></span>

println(download("2.9.3"))
println(download("2.10.1-RC2"))
