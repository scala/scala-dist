import sbt._
import org.clapper.sbtplugins.IzPackPlugin
import org.clapper.sbtplugins.izpack._

class InstallerProject(info: ProjectInfo)
        extends DefaultProject(info)
        with IzPackPlugin
{ outer =>
  
  val buildDir    = info.projectPath / outputDirectoryName
  val URL         = "http://scala-lang.org/"
  val VERSION     = buildScalaVersion
  val PREFIX      = "scala-" + VERSION
  val RESOURCEDIR = buildDir / "izpack" / PREFIX ##
  val SBAZDIR     = buildDir / "install" / "sbaz-docs" ##
  val WINDIR      = buildDir / "windows" / "latest" ##
  val UNIXDIR     = buildDir / "unix" / "latest" ##

  private def installerInputXML = info.projectPath / "src" / "izpack" / "install-nsc.xml" asFile
  private def buildInstaller = {
    val installerJar = info.projectPath / outputDirectoryName / ("scala-%s-installer.jar" format buildScalaVersion)
    izpackMakeInstaller(installConfig, installerJar)
  }
  lazy val installer = (
    task { buildInstaller ; None }
    . dependsOn(packageAction, docAction)
    . describedAs("Build installer.")
  )
  
  lazy val installConfig = new IzPackConfig("target" / "install", log) {
    languages = List("eng")

    new Info {
      appName               = "scala"
      appVersion            = VERSION
      url                   = URL
      javaVersion           = "1.6"
      writeInstallationInfo = true
      customXML = <run-privileged condition="izpack.windowsinstall.vista|izpack.windowsinstall.7"/>
    }
    new Variables {
      variable("InstallerFrame.logfilePath", "default")
    }
    customXML = {
      <native type="izpack" name="ShellLink.dll"/>
      <native type="izpack" name="ShellLink_x64.dll"/>
    }
    new GuiPrefs {
      resizable = true
      height = 460
      width  = 600

      new LookAndFeel("metouia") {
        onlyFor(Unix)
      }
      new LookAndFeel("looks") {
        onlyFor(Windows)
        params = Map("variant" -> "extwin")
      }
    }

    new Resources {
      private implicit def mkResource(kv: (String, String)): Resource = new Resource {
        id     = kv._1
        source = Path fromFile new java.io.File(RESOURCEDIR.toString, kv._2)
      }
      
      List[Resource](
        "installer.langsel.img"        -> "images/Splash.png",
        "Installer.image"              -> "images/install.png",
        "HTMLInfoPanel.info_eng"       -> "locales/INFO_en.html",
        "HTMLInfoPanel.info_fra"       -> "locales/INFO_fr.html",
        "HTMLInfoPanel.info_deu"       -> "locales/INFO_de.html",
        "HTMLLicencePanel.licence_eng" -> "locales/LICENSE_en.html",
        "HTMLLicencePanel.licence_fra" -> "locales/LICENSE_fr.html",
        "HTMLLicencePanel.licence_deu" -> "locales/LICENSE_de.html",
        "ProcessPanel.Spec.xml"        -> "izpack_process.xml",
        "shortcutSpec.xml"             -> "izpack_shortcut.xml",
        "TargetPanel.dir.macosx"       -> "targets/path_macosx.txt",
        "CustomLangpack.xml_eng"       -> "locales/myPacksLang.xml_eng"
      )
    }
    new Panels {
      new Panel("HelloPanel")
      new Panel("HTMLInfoPanel")
      new Panel("HTMLLicencePanel")
      new Panel("TargetPanel")
      new Panel("PacksPanel")
      new Panel("InstallPanel")
      new Panel("ProcessPanel")
      new Panel("ShortcutPanel")
      new Panel("SimpleFinishPanel")
    }
    new Packs {
      new Pack("Software Package Installation") {
        required = true
        description = "Installing the Scala software."
        new FileSet(UNIXDIR.descendentsExcept("*", "meta"), "$INSTALL_PATH") {
          onlyFor(Unix, MacOSX)
        }
        new FileSet(SBAZDIR ** ("meta" || "doc/scala-devel-docs" || "doc/scala-documentation"), "$INSTALL_PATH") {
        }
        // scripts are executable files
        List("fsc", "sbaz", "sbaz-setup", "scala", "scalac", "scalap", "scaladoc") map { name =>
          new Executable("$INSTALL_PATH/bin/" + name) {
            onlyFor(Unix, MacOSX)
          }
        }
        // copy the lib and src directories from unixdir, in order not to include
        // the same large files twice
        new FileSet(UNIXDIR ** ("lib" || "src"), "$INSTALL_PATH") {
          onlyFor(Windows)
        }
        // the rest of the windows distribution
        new FileSet(WINDIR.descendentsExcept("*", "lib" || "src" || "meta"), "$INSTALL_PATH") {
          onlyFor(Windows)
        }
        new FileSet(RESOURCEDIR / "registry" / "bin", "$INSTALL_PATH/Uninstaller") {
          onlyFor(Windows)
        }
        new Executable("$JAVA_HOME\\bin\\java") {
          onlyFor(Windows)
          override val stage = "uninstall"
          override val args = List(
            "-Djava.library.path=$INSTALL_PATH\\Uninstaller",
            "-cp",
            "$INSTALL_PATH\\Uninstaller\\registryAny.jar;$INSTALL_PATH\\Uninstaller\\setenv.jar",
            "Main",
            VERSION
          )          
        }
      }
    }
  }
  // <target name="build.izpack" depends="init"> <!-- />build.install-docs"> -->
  //   <taskdef
  //     name="izpack"
  //     classname="com.izforge.izpack.ant.IzPackTask"
  //     classpath="${izpack.jar}"/>
  // 
  //   <!-- Copy binary files without using filter -->
  //   <copy todir="${build-izpack.dir}/scala-${version.number}">
  //     <fileset dir="${src.dir}/izpack"
  //              excludes="**/*.html, **/*.xml"/>
  //   </copy>
  // 
  //   <!-- Caution: Copy operation using filters will corrupt
  //        binary files, see "Core Types". -->
  //   <copy todir="${build-izpack.dir}/scala-${version.number}">
  //     <fileset dir="${src.dir}/izpack"
  //              includes="**/*.html, **/*.xml"/>
  //     <filterset>
  //       <filter token="UNIXDIR"     value="${build-unix.dir}/latest"/>
  //       <filter token="WINDIR"      value="${build-win.dir}/latest"/>
  //       <filter token="SBAZDIR"     value="${build-install.dir}/sbaz-docs"/>
  //       <filter token="RESOURCEDIR" value="${build-izpack.dir}/scala-${version.number}"/>
  //       <filter token="PREFIX"      value="scala-${version.number}"/>
  //       <filter token="VERSION"     value="${version.number}"/>
  //       <filter token="URL"         value="http://scala-lang.org/"/>
  //     </filterset>
  //   </copy>
  //   <izpack
  //     input="${build-izpack.dir}/scala-${version.number}/install-nsc.xml"
  //     output="${build-izpack.dir}/scala-${version.number}-installer.jar"
  //     installerType="standard" basedir="${basedir}"
  //     izPackDir="${build-izpack.dir}/scala-${version.number}"/>
  // 
  //   <checksum
  //     file="${build-izpack.dir}/scala-${version.number}-installer.jar"
  //     fileext=".md5"/>
  // </target>
}