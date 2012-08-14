import sbt._
 
object Build extends sbt.Build {
  import Keys._
  import sbtbuildinfo.Plugin._
 
  lazy val root = Project("root", file("."), settings = Defaults.defaultSettings,
    aggregate = Seq(app))
  lazy val app = Project("app", file("."), settings = appSettings)
 
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    organization := "com.example"
  )
  
  lazy val check = TaskKey[Unit]("check")

  lazy val appSettings = buildSettings ++ buildInfoSettings ++ Seq(
    name := "sbt-buildinfo-example-app",
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[Scoped](name, projectID in "root", version, scalaVersion, sbtVersion),
    buildInfoPackage := "hello",
    check <<= (sourceManaged in Compile) map { (dir) =>
      val f = dir / ("%s.scala" format "BuildInfo")
      val lines = scala.io.Source.fromFile(f).getLines.toList
      lines match {
        case """package hello""" ::
             """""" ::
             """object BuildInfo {""" ::
             """  val name = "sbt-buildinfo-example-app"""" ::
             """  val projectId = "root:root:0.1-SNAPSHOT"""" ::
             """  val version = "0.1"""" ::
             """  val scalaVersion = "2.9.2"""" ::
             """  val sbtVersion = "0.12.0"""" :: 
             """}""" :: Nil =>
        case _ => sys.error("unexpeted output: " + lines.toString)
      }
      ()
    }
  )
}
