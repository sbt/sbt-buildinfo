lazy val check = taskKey[Unit]("checks this plugin")

lazy val root = (project in file(".")).
  settings(
    name := "helloworld",
    version := "0.1",
    scalaVersion := "2.10.2",
    buildInfoKeys := Seq(
      name,
      scalaVersion
    ),
    buildInfoPackage := "hello",
    buildInfoOptions ++= Seq(BuildInfoOption.ToJson, BuildInfoOption.ToMap),
    homepage := Some(url("http://example.com")),
    licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE")),
    check := {
      val f = (sourceManaged in Compile).value / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
      val lines = scala.io.Source.fromFile(f).getLines.toList
      lines match {
        case """package hello""" ::
             """""" ::
             """import java.io.File""" ::
             """import java.net.URL""" ::
             """""" ::
             """/** This object was generated by sbt-buildinfo. */""" ::
             """case object BuildInfo {""" ::
             """  /** The value is "helloworld". */"""::
             """  val name: String = "helloworld"""" ::
             """  /** The value is "2.10.2". */""" ::
             """  val scalaVersion: String = "2.10.2"""" ::
             """  override val toString: String = "name: %s, scalaVersion: %s" format (name, scalaVersion)""" ::
             """  val toMap: Map[String, Any] = Map[String, Any](""" ::
             """    "name" -> name,""" ::
             """    "scalaVersion" -> scalaVersion)""" ::
             "" ::
             """  val toJson: String = toMap.map(i => "\"" + i._1 + "\":\"" + i._2 + "\"").mkString("{", ", ", "}")""" ::
             """}""" :: Nil =>
        case _ => sys.error("unexpected output: \n" + lines.mkString("\n"))
      }
      ()
    }
  )
