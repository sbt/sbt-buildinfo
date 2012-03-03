name := "helloworld"

version := "0.1"

seq(buildInfoSettings: _*)

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[Scoped](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "hello"

TaskKey[Unit]("check") <<= (sourceManaged in Compile) map { (dir) =>
  val f = dir / ("%s.scala" format "BuildInfo")
  val lines = scala.io.Source.fromFile(f).getLines.toList
  lines match {
    case """package hello""" ::
         """""" ::
         """object BuildInfo {""" ::
         """  val name = "helloworld"""" ::
         """  val version = "0.1"""" ::
         """  val scalaVersion = "2.9.1"""" ::
         """  val sbtVersion = "0.11.2"""" :: 
         """}""" :: Nil =>
    case _ => sys.error("unexpeted output: " + lines.toString)
  }
  ()
}
