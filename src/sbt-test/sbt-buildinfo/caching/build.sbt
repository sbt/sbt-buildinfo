name := "helloworld"

version := "0.1"

scalaVersion := "2.10.2"

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq(name, version)

buildInfoPackage := "hello"

homepage := Some(url("http://example.com"))

licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

TaskKey[Unit]("check") <<= (sourceManaged in Compile) map { (dir) =>
  val f = dir / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
  val lines = scala.io.Source.fromFile(f).getLines.toList
  lines match {
    case """package hello""" ::
         """""" ::
         """case object BuildInfo {""" ::
         """  val name = "helloworld"""" ::
         """  val version = "0.1"""" ::
         """  override val toString = "name: %s, version: %s" format (name, version)""" ::
         """}""" :: Nil =>
    case _ => sys.error("unexpected output: \n" + lines.mkString("\n"))
  }
  ()
}
