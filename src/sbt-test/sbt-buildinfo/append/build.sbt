name := "helloworld"

organization := "com.eed3si9n"

version := "0.1"

scalaVersion := "2.10.2"

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys ++= Seq[BuildInfoKey](name, organization, version, scalaVersion,
  libraryDependencies, libraryDependencies in Test)

buildInfoKeys += BuildInfoKey(resolvers)

buildInfoPackage := "hello"

homepage := Some(url("http://example.com"))

licenses := Seq("MIT License" -> url("https://github.com/sbt/sbt-buildinfo/blob/master/LICENSE"))

resolvers ++= Seq("Sonatype Public" at "https://oss.sonatype.org/content/groups/public")

TaskKey[Unit]("check") <<= (sourceManaged in Compile) map { (dir) =>
  val f = dir / "sbt-buildinfo" / ("%s.scala" format "BuildInfo")
  val lines = scala.io.Source.fromFile(f).getLines.toList
  lines match {
    case """package hello""" ::
         """""" ::
         """case object BuildInfo {""" ::
         """  val name = "helloworld"""" ::
         """  val version = "0.1"""" ::
         """  val scalaVersion = "2.10.2"""" ::
         """  val sbtVersion = "0.13.0-Beta2"""" ::
         """  val organization = "com.eed3si9n"""" ::
         """  val libraryDependencies = Seq("org.scala-lang:scala-library:2.10.2")""" ::
         """  val test_libraryDependencies = Seq("org.scala-lang:scala-library:2.10.2")""" ::
         """  val resolvers = Seq("Sonatype Public: https://oss.sonatype.org/content/groups/public")""" ::
         """}""" :: Nil =>
    case _ => sys.error("unexpected output: \n" + lines.mkString("\n"))
  }
  ()
}
