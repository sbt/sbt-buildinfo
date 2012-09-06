sbt-buildinfo
=============

*I know this because build.sbt knows this.*

sbt-buildinfo generates Scala source from your build definitions.

Latest Stable
-------------

```scala
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.0")
```

Usage
-----

Add the following in your `build.sbt`:

```scala
buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "hello"
```

(__Note__: in version 0.1.2, this was `Seq[Scoped]` instead!)

When you reload the settings and compile, this generates the following:

```scala
package hello

object BuildInfo {
  val name = "helloworld"
  val version = "0.1-SNAPSHOT"
  val scalaVersion = "2.9.2"
  val sbtVersion = "0.12.0"
}
```

Customize `buildInfoKeys` by adding whatever keys. You can use `BuildInfoKey.map` to change the generated field
name and value, add new fields with tuples, or add new fields with values computed at build-time:

```scala
buildInfoKeys ++= Seq[BuildInfoKey](
  resolvers,
  libraryDependencies in Test,
  "custom" -> 1234,
  BuildInfo.map(name) { case (k, v) => "project" + k.capitalize -> v.capitalize },
  "buildTime" -> () -> System.currentTimeMillis
)
```

This generates:

```scala
  val resolvers = Seq("Sonatype Public: https://oss.sonatype.org/content/groups/public")
  val test_libraryDependencies = Seq("org.scala-lang:scala-library:2.9.1", ...)
  val custom = 1234
  val projectName = "Helloworld"
  val buildTime = 1346906092160L
```

Tasks can be added only if they do not depend on `sourceGenerators`. Otherwise, it will cause an infinite loop.

Here's how to change the generated the object name:

```scala
buildInfoObject := "Info"
```

This changes the generated object to `object Info`. Changing the object name is optional, but to avoid name clash with other jars, package name should be unique.

### build number

A build number can be generated as follows. Note that cross building against multiple Scala would each generate a new number.

```scala
buildInfoKeys += buildInfoBuildNumber
```

License
-------

MIT License
