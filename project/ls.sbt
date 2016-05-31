resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  Opts.resolver.sonatypeReleases
)

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")
