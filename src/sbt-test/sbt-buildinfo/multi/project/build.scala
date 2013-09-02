import sbt._
 
object Build extends sbt.Build {
  import Keys._
  import sbtbuildinfo.Plugin._
 
  lazy val root = Project("root", file("."), settings = Defaults.defaultSettings,
    aggregate = Seq(app))
  lazy val app = Project("app", file("app"), settings = appSettings)
 
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    organization := "com.example",
    homepage := Some(url("http://example.com")),
    scalaVersion := "2.10.2"
  )
  
  lazy val check = TaskKey[Unit]("check")

  lazy val appSettings = buildSettings ++ buildInfoSettings ++ Seq(
    name := "sbt-buildinfo-example-app",
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq(name,
                         projectID in "root",
                         version,
                         BuildInfoKey.map(homepage) { case (n, opt) => n -> opt.get },
                         scalaVersion,
                         sbtVersion),
    buildInfoPackage := "hello",
    check <<= (sourceManaged in Compile) map { (dir) =>
      val f = dir / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
      val lines = scala.io.Source.fromFile(f).getLines.toList
      lines match {
        case """package hello""" ::
             """""" ::
             """case object BuildInfo {""" ::
             """  val name = "sbt-buildinfo-example-app"""" ::
             """  val projectId = "root:root:0.1-SNAPSHOT"""" ::
             """  val version = "0.1"""" ::
             """  val homepage = new java.net.URL("http://example.com")""" ::
             """  val scalaVersion = "2.10.2"""" ::
             """  val sbtVersion = "0.13.0"""" ::
             """}""" :: Nil =>
        case _ => sys.error("unexpected output: " + lines.mkString("\n"))
      }
      ()
    }
  )
}
