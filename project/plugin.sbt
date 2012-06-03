resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)

resolvers += Resolver.url("Typesafe snapshot repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-snapshots/"))(Resolver.defaultIvyPatterns)

// libraryDependencies <+= (sbtVersion) { sv =>
//   "org.scala-sbt" %% "scripted-plugin" % sv
//   // "org.scala-sbt" % "scripted-plugin" % sv
// }

resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1")

// addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.6.1-SNAPSHOT")
addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.6.0")
