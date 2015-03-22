lazy val root = (project in file(".")).
  settings(
    sbtPlugin := true,
    name := "sbt-buildinfo",
    organization := "com.eed3si9n",
    version := "0.4.0-SNAPSHOT",
    // sbtVersion in Global := "0.13.0" 
    // scalaVersion in Global := "2.10.2"
    scalacOptions := Seq("-unchecked", "-deprecation"),
    description := "sbt plugin to generate build info",
    licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE")),
    publishArtifact in (Compile, packageBin) := true,
    publishArtifact in (Test, packageBin) := false,
    publishArtifact in (Compile, packageDoc) := true,
    publishArtifact in (Compile, packageSrc) := true,
    publishMavenStyle := false,
    publishTo := {
      val repoId = if (isSnapshot.value) "snapshots" else "releases"
      Some(Resolver.sbtPluginRepo(repoId))
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")
  ).
  settings(lsSettings: _*).
  settings(
    LsKeys.tags in LsKeys.lsync := Seq("sbt", "codegen")
  )
