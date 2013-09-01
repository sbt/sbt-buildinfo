sbtPlugin := true

name := "sbt-buildinfo"

organization := "com.eed3si9n"

version := "0.2.6-SNAPSHOT"

sbtVersion in Global := "0.13.0"

scalaVersion in Global := "2.10.2"

description := "sbt plugin to generate build info"

licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := true

publishArtifact in (Compile, packageSrc) := true

publishMavenStyle := false

publishTo <<= (version) { version: String =>
   val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
   val (name, u) = if (version.contains("-SNAPSHOT")) ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
                   else ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
   Some(Resolver.url(name, url(u))(Resolver.ivyStylePatterns))
}

credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")

lsSettings

LsKeys.tags in LsKeys.lsync := Seq("sbt", "codegen")

// CrossBuilding.crossSbtVersions := Seq("0.11.3", "0.11.2" ,"0.12.0-Beta2")

ScriptedPlugin.scriptedSettings
// CrossBuilding.scriptedSettings

scriptedLaunchOpts ++= Seq("-Xmx1024M", "-XX:MaxPermSize=256M")
