ThisBuild / organization := "com.eed3si9n"
ThisBuild / dynverSonatypeSnapshots := true
ThisBuild / version := {
  val orig = (ThisBuild / version).value
  if (orig.endsWith("-SNAPSHOT")) "0.11.0-SNAPSHOT"
  else orig
}

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(pomConsistency2021DraftSettings)
  .settings(
    name := "sbt-buildinfo",
    scalacOptions := Seq("-Xlint", "-Xfatal-warnings", "-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    scalacOptions += "-language:experimental.macros",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Xss4M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8"
      }
    }
  )

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/sbt/sbt-buildinfo"),
    "scm:git@github.com:sbt/sbt-buildinfo.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "eed3si9n",
    name = "Eugene Yokota",
    email = "@eed3si9n",
    url = url("https://eed3si9n.com/")
  )
)
ThisBuild / description := "sbt plugin to generate build info"
ThisBuild / licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))
ThisBuild / pomIncludeRepository := { _ =>
  false
}
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
ThisBuild / homepage := Some(url("https://github.com/sbt/sbt-buildinfo"))

// See https://eed3si9n.com/pom-consistency-for-sbt-plugins
lazy val pomConsistency2021Draft = settingKey[Boolean]("experimental")

/**
 * this is an unofficial experiment to re-publish plugins with better Maven compatibility
 */
def pomConsistency2021DraftSettings: Seq[Setting[_]] = Seq(
  pomConsistency2021Draft := Set("true", "1")(sys.env.get("POM_CONSISTENCY").getOrElse("false")),
  moduleName := {
    if (pomConsistency2021Draft.value)
      sbtPluginModuleName2021Draft(moduleName.value,
        (pluginCrossBuild / sbtBinaryVersion).value)
    else moduleName.value
  },
  projectID := {
    if (pomConsistency2021Draft.value) sbtPluginExtra2021Draft(projectID.value)
    else projectID.value
  },
)

def sbtPluginModuleName2021Draft(n: String, sbtV: String): String =
  s"""${n}_sbt${if (sbtV == "1.0") "1" else if (sbtV == "2.0") "2" else sbtV}"""

def sbtPluginExtra2021Draft(m: ModuleID): ModuleID =
  m.withExtraAttributes(Map.empty)
   .withCrossVersion(CrossVersion.binary)
