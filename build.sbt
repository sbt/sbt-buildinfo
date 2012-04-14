sbtPlugin := true

name := "sbt-buildinfo"

organization := "com.eed3si9n"

version := "0.1.1-SNAPSHOT"

description := "sbt plugin to generate build info"

licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

ScriptedPlugin.scriptedSettings

lsSettings

LsKeys.tags in LsKeys.lsync := Seq("sbt", "codegen")

(externalResolvers in LsKeys.lsync) := Seq(
  "sbt-plugin-releases" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases")

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishMavenStyle := false

publishTo <<= (version) { version: String =>
   val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
   val (name, u) = if (version.contains("-SNAPSHOT")) ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
                   else ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
   Some(Resolver.url(name, url(u))(Resolver.ivyStylePatterns))
}

credentials += Credentials(Path.userHome / ".ivy2" / ".sbtcredentials")
