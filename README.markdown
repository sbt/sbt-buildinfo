sbt-buildinfo
=============

*I know this because build.sbt knows this.*

sbt-buildinfo generates Scala source from your build definitions.

Latest
------

```scala
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.1.2")
```

Usage
-----

Add the following in your `build.sbt`:

```scala
buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[Scoped](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "hello"
```

When you reload the settings and compile, this generates the following:

```scala
package hello

object BuildInfo {
  val name = "helloworld"
  val version = "0.1-SNAPSHOT"
  val scalaVersion = "2.9.1"
  val sbtVersion = "0.11.2"
}
```

Customize `buildInfoKeys` by adding whatever keys.

```scala
buildInfoKeys ++= Seq[Scoped](resolvers, libraryDependencies in Test)
```

This generates:

```scala
  val resolvers = Seq("Sonatype Public: https://oss.sonatype.org/content/groups/public")
  val test_libraryDependencies = Seq("org.scala-lang:scala-library:2.9.1", ...)
```

Tasks can be added only if they do not depend on `sourceGenerators`. Otherwise, it will cause an infinite loop.

Here's how to change the generated the object name:

```scala
buildInfoObject  := "Info"
```

This change to `object Info`. Changing the object name is optional, but to avoid name clash with other jars, package name should be changed.

### build number

A build number can be generated as follows. Note that cross building against multiple Scala would each generate new number.

```scala
buildInfoKeys += buildInfoBuildNumber
```

License
-------

MIT License
