name := "helloworld"

version := "0.1"

seq(buildInfoSettings: _*)

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq(name, BuildInfo.map(version) { case (n, v) => "projectVersion" -> v.toDouble }, scalaVersion, sbtVersion, homepage, licenses, isSnapshot)

buildInfoPackage := "hello"

homepage := Some(url("http://example.com"))

licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

TaskKey[Unit]("check") <<= (sourceManaged in Compile) map { (dir) =>
  val f = dir / ("%s.scala" format "BuildInfo")
  val lines = scala.io.Source.fromFile(f).getLines.toList
  lines match {
    case """package hello""" ::
         """""" ::
         """object BuildInfo {""" ::
         """  val name = "helloworld"""" ::
         """  val projectVersion = 0.1""" ::
         """  val scalaVersion = "2.9.2"""" ::
         """  val sbtVersion = "0.12.0"""" ::
         """  val homepage: Option[java.net.URL] = Some(new java.net.URL("http://example.com"))""" ::
         """  val licenses = Seq(("MIT License" -> new java.net.URL("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE")))""" ::
         """  val isSnapshot = false""" ::
         """}""" :: Nil =>
    case _ => sys.error("unexpected output: \n" + lines.mkString("\n"))
  }
  ()
}
