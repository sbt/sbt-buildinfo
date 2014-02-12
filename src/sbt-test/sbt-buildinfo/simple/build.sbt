name := "helloworld"

version := "0.1"

scalaVersion := "2.10.2"

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq(
  name,
  BuildInfoKey.map(version) { case (n, v) => "projectVersion" -> v.toDouble },
  scalaVersion,
  ivyXML,
  homepage,
  licenses,
  isSnapshot,
  "year" -> 2012,
  "sym" -> 'Foo,
  BuildInfoKey.action("buildTime") { 1234L },
  target
)

buildInfoPackage := "hello"

homepage := Some(url("http://example.com"))

licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

val check = taskKey[Unit]("checks this plugin")

check := {
  val f = (sourceManaged in Compile).value / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
  val lines = scala.io.Source.fromFile(f).getLines.toList
  lines match {
    case """package hello""" ::
         """""" ::
         """case object BuildInfo {""" ::
         """  val name = "helloworld"""" ::
         """  val projectVersion = 0.1""" ::
         """  val scalaVersion = "2.10.2"""" ::
         """  val ivyXml = Seq()""" ::
         """  val homepage: Option[java.net.URL] = Some(new java.net.URL("http://example.com"))""" ::
         """  val licenses = Seq(("MIT License" -> new java.net.URL("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE")))""" ::
         """  val isSnapshot = false""" ::
         """  val year = 2012""" ::
         """  val sym = 'Foo""" ::
         """  val buildTime = 1234L""" ::
         targetInfo :: // """
         """}""" :: Nil if (targetInfo contains "val target = new java.io.File(") =>
    case _ => sys.error("unexpected output: \n" + lines.mkString("\n"))
  }
  ()
}
