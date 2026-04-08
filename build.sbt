import Dependencies.*

ThisBuild / organization := "com.eed3si9n"
ThisBuild / version := {
  val orig = (ThisBuild / version).value
  if (orig.endsWith("-SNAPSHOT")) "0.11.0-SNAPSHOT"
  else orig
}
val scala3 = "3.8.2"
ThisBuild / scalaVersion := scala3

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-buildinfo",
    scalacOptions := {
      scalaBinaryVersion.value match {
        case "2.12" => Seq("-Xsource:3", "-Xfatal-warnings", "-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-release:8")
        case _      => Seq("-Vdebug")
      }
    },
    scalacOptions += "-language:experimental.macros",
    libraryDependencies ++= {
      scalaBinaryVersion.value match {
        case "2.12" => "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided :: Nil
        case _      => manifesto :: Nil
      }
    },
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Xss4M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    crossScalaVersions := List(scala3, "2.12.20"),
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.5.8"
        case _      => "2.0.0-RC11"
      }
    },
    scriptedSbt := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.10.7"
        case _      => (pluginCrossBuild / sbtVersion).value
      }
    },
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
