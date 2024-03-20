import sbtbuildinfo.ScalaCaseClassRenderer

lazy val check = taskKey[Unit]("checks this plugin")

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.12.12"
ThisBuild / homepage := Some(url("http://example.com"))
ThisBuild / licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "helloworld",
    buildInfoKeys := Seq(
      name,
      BuildInfoKey.map(version) { case (n, v) => "projectVersion" -> v.toDouble },
      scalaVersion,
      ivyXML,
      homepage,
      licenses,
      apiMappings,
      isSnapshot,
      "year" -> 2012,
      "sym" -> 'Foo,
      BuildInfoKey.action("buildTime") { 1234L },
      target),
    buildInfoOptions ++= Seq(
      BuildInfoOption.ToJson,
      BuildInfoOption.ToMap,
      BuildInfoOption.Traits("traits.MyCustomTrait"),
    ),
    buildInfoRenderFactory := ScalaCaseClassRenderer.apply,
    buildInfoPackage := "hello",
    scalacOptions ++= Seq("-Ywarn-unused-import", "-Xfatal-warnings"),
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
    check := {
      val f = (sourceManaged in Compile).value / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
      val lines = scala.io.Source.fromFile(f).getLines.toList
      lines match {
        case """// $COVERAGE-OFF$""" ::
          """package hello""" ::
          """""" ::
          """/** This file was generated by sbt-buildinfo. */""" ::
          """case class BuildInfo(""" ::
          """  name: String,""" ::
          """  projectVersion: scala.Any,""" ::
          """  scalaVersion: String,""" ::
          """  ivyXML: scala.xml.NodeSeq,""" ::
          """  homepage: scala.Option[java.net.URL],""" ::
          """  licenses: scala.collection.immutable.Seq[(String, java.net.URL)],""" ::
          """  apiMappings: Map[java.io.File, java.net.URL],""" ::
          """  isSnapshot: scala.Boolean,""" ::
          """  year: scala.Int,""" ::
          """  sym: scala.Symbol,""" ::
          """  buildTime: scala.Long,""" ::
          """  target: java.io.File""" ::
          """) extends traits.MyCustomTrait {""" ::
          """""" ::
          """  val toMap: Map[String, scala.Any] = Map[String, scala.Any](""" ::
          """    "name" -> name,""" ::
          """    "projectVersion" -> projectVersion,""" ::
          """    "scalaVersion" -> scalaVersion,""" ::
          """    "ivyXML" -> ivyXML,""" ::
          """    "homepage" -> homepage,""" ::
          """    "licenses" -> licenses,""" ::
          """    "apiMappings" -> apiMappings,""" ::
          """    "isSnapshot" -> isSnapshot,""" ::
          """    "year" -> year,""" ::
          """    "sym" -> sym,""" ::
          """    "buildTime" -> buildTime,""" ::
          """    "target" -> target)""" ::
          """""" ::
          """  private def quote(x: scala.Any): String = "\"" + x + "\""""" ::
          """  private def toJsonValue(value: scala.Any): String = {""" ::
          """    value match {""" ::
          """      case elem: scala.collection.Seq[_] => elem.map(toJsonValue).mkString("[", ",", "]")""" ::
          """      case elem: scala.Option[_] => elem.map(toJsonValue).getOrElse("null")""" ::
          """      case elem: scala.collection.Map[_, scala.Any] => elem.map {""" ::
          """        case (k, v) => toJsonValue(k.toString) + ":" + toJsonValue(v)""" ::
          """      }.mkString("{", ", ", "}")""" ::
          """      case d: scala.Double => d.toString""" ::
          """      case f: scala.Float => f.toString""" ::
          """      case l: scala.Long => l.toString""" ::
          """      case i: scala.Int => i.toString""" ::
          """      case s: scala.Short => s.toString""" ::
          """      case bool: scala.Boolean => bool.toString""" ::
          """      case str: String => quote(str)""" ::
          """      case other => quote(other.toString)""" ::
          """    }""" ::
          """  }""" ::
          """""" ::
          """  val toJson: String = toJsonValue(toMap)""" ::
          """}""" ::
          """""" ::
          """case object BuildInfo {""" ::
          """  def apply(): BuildInfo = new BuildInfo(""" ::
          """    name = "helloworld",""" ::
          """    projectVersion = 0.1,""" ::
          """    scalaVersion = "2.12.12",""" ::
          """    ivyXML = scala.xml.NodeSeq.Empty,""" ::
          """    homepage = scala.Some(new java.net.URL("http://example.com")),""" ::
          """    licenses = scala.collection.immutable.Seq(("MIT License" -> new java.net.URL("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))),""" ::
          """    apiMappings = Map(),""" ::
          """    isSnapshot = false,""" ::
          """    year = 2012,""" ::
          """    sym = scala.Symbol("Foo"),""" ::
          """    buildTime = 1234L,""" ::
          targetInfo ::
          """  val get = apply()""" ::
          """  val value = apply()""" ::
          """}""" ::
          """// $COVERAGE-ON$""" :: Nil if (targetInfo contains "target = new java.io.File(") =>
        case _ => sys.error("unexpected output: \n" + lines.mkString("\n"))
      }
      ()
    }
  )