
Upgrading from 0.3.x to 0.4.0
-----------------------------

### sbt 0.13.5 or above

Auto plugins are available only for sbt 0.13.5 and above.

### Upgrading with multi-project build.sbt

If you are using multi-project `build.sbt` (before):

```scala
lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.example",
  scalaVersion := "2.10.1"
)

lazy val app = (project in file("app")).
  settings(commonSettings: _*).
  settings(buildInfoSettings: _*).
  settings(
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "hello"
    // your settings here
  )
```

1. Remove `settings(buildInfoSettings: _*).`.
2. Remove `sourceGenerators in Compile <+= buildInfo,`
2. Add `enablePlugins(BuildInfoPlugin)`.

Here's how build.sbt looks now:

```scala
lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.example",
  scalaVersion := "2.10.1"
)

lazy val app = (project in file("app")).
  enablePlugins(BuildInfoPlugin).
  settings(commonSettings: _*).
  settings(
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "hello",
    // your settings here
  )
```
