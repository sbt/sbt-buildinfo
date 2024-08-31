ThisBuild / organization := "com.eed3si9n"

ThisBuild / version := {
  val orig = (ThisBuild / version).value
  if (orig.endsWith("-SNAPSHOT")) "0.11.0-SNAPSHOT"
  else orig
}

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-buildinfo",
    scalacOptions := Seq("-Xlint", "-Xfatal-warnings", "-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    scalacOptions += "-language:experimental.macros",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Xss4M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.5.8"
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
ThisBuild / homepage := Some(url("https://github.com/sbt/sbt-buildinfo"))
