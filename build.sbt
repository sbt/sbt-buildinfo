lazy val commonSettings: Seq[Setting[_]] = Seq(
  git.baseVersion in ThisBuild := "0.9.0",
  organization in ThisBuild := "com.eed3si9n"
)

lazy val root = (project in file(".")).
  enablePlugins(GitVersioning, ScriptedPlugin).
  settings(
    commonSettings,
    sbtPlugin := true,
    name := "sbt-buildinfo",
    // sbtVersion in Global := "0.13.0" 
    // scalaVersion in Global := "2.10.2"
    scalacOptions := Seq("-Xfuture", "-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    scalacOptions += "-language:experimental.macros",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
    description := "sbt plugin to generate build info",
    homepage := Some(url("https://github.com/sbt/sbt-buildinfo")),
    licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE")),
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false
  )
