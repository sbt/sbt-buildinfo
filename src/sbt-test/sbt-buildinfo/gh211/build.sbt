lazy val check = taskKey[Unit]("checks this plugin")

lazy val a1 = project.settings(scalaVersion := "2.13.15")

lazy val a2 = project.settings(scalaVersion := "3.3.4")

lazy val a3 = project
  .enablePlugins(BuildInfoPlugin)
  .settings(
    Compile / buildInfoKeys := List[BuildInfoKey](
      BuildInfoKey.map((a1 / scalaVersion): SettingKey[String])("scalaVersion_a1" -> _._2),
      BuildInfoKey.map((a2 / scalaVersion): SettingKey[String])("scalaVersion_a2" -> _._2),
    ),
    check := {
      val sv = scalaVersion.value
      val _ = (Compile / compile).value
      val f = (Compile / sourceManaged).value / "sbt-buildinfo" / ("BuildInfo.scala")
      val lines = scala.io.Source.fromFile(f).getLines.toList

      assert(lines.contains("  val scalaVersion_a2: String = \"3.3.4\""), lines.toString)
    },
  )
