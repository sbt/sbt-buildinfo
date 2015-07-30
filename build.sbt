lazy val commonSettings: Seq[Setting[_]] = Seq(
  git.baseVersion in ThisBuild := "0.5.0",
  organization in ThisBuild := "com.eed3si9n"
)

lazy val root = (project in file(".")).
  enablePlugins(GitVersioning).
  settings(
    commonSettings,
    sbtPlugin := true,
    name := "sbt-buildinfo",
    // sbtVersion in Global := "0.13.0" 
    // scalaVersion in Global := "2.10.2"
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    description := "sbt plugin to generate build info",
    licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))
  ).
  settings(lsSettings: _*).
  settings(
    LsKeys.tags in LsKeys.lsync := Seq("sbt", "codegen")
  )
