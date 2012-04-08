package sbtbuildinfo

import sbt._

object Plugin extends sbt.Plugin {
  import Keys._
  import Project.Initialize

  lazy val buildInfo        = TaskKey[Seq[File]]("buildinfo")
  lazy val buildInfoObject  = SettingKey[String]("buildinfo-object")
  lazy val buildInfoPackage = SettingKey[String]("buildinfo-package") 
  lazy val buildInfoKeys    = SettingKey[Seq[Scoped]]("buildinfo-keys")

  case class BuildInfo(dir: File, obj: String, pkg: String, keys: Seq[Scoped],
    proj: ProjectRef, state0: State) {
    private var _state: State = state0 
    private def extracted = Project.extract(_state)

    def file: File = {
      val f = dir / ("%s.scala" format obj)
      val lines =
        List("package %s" format pkg,
          "",
          "object %s {" format obj) :::
        (keys.toList.distinct map { line(_) }).flatten :::
        List("}")
      IO.write(f, lines.mkString("\n"))
      f
    }

    private def line(key: Scoped): Option[String] =
      value(key) map { x => "  val %s = %s" format (ident(key), quote(x)) }
    private def value(scoped: Scoped): Option[Any] = {
      val scope = if (scoped.scope.project == This) scoped.scope in (proj)
                  else scoped.scope
      scoped match {
        case key: SettingKey[_] => extracted getOpt (key in scope)
        case key: TaskKey[_]    =>
          val (s, x) = extracted runTask (key in scope, _state)
          Some(x)
        case _ => None
      }      
    }

    private def ident(key: Scoped): String =
      (key.scope.config.toOption match {
        case None => ""
        case Some(ConfigKey("compile")) => ""
        case Some(ConfigKey(x)) => x + "_"
      }) + 
      (key.scope.task.toOption match {
        case None => ""
        case Some(x) => x.label + "_"
      }) + 
      (key.key.label.split("-").toList match {
        case Nil => ""
        case x :: xs => x + (xs map {_.capitalize}).mkString("")
      })
    private def quote(v: Any): String = v match {
      case x: Int => x.toString
      case x: Long => x.toString + "L"
      case x: Double => x.toString
      case x: Boolean => x.toString
      case node: scala.xml.NodeSeq => node.toString
      case (k, v) => "(%s -> %s)" format(quote(k), quote(v))
      case mp: Map[_, _] => mp.toList.map(quote(_)).mkString("Map(", ", ", ")")
      case seq: Seq[_]   => seq.map(quote(_)).mkString("Seq(", ", ", ")")
      case op: Option[_] => op map { x => "Some(" + quote(x) + ")" } getOrElse {"None"}
      case s => "\"%s\"" format s.toString
    }
  }

  lazy val buildInfoSettings: Seq[Project.Setting[_]] = Seq(
    buildInfo <<= (sourceManaged in Compile,
        buildInfoObject, buildInfoPackage, buildInfoKeys, thisProjectRef, state) map {
      (dir, obj, pkg, keys, ref, state) =>
      Seq(BuildInfo(dir, obj, pkg, keys, ref, state).file)
    },
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoKeys    := Seq[Scoped](name, version, scalaVersion, sbtVersion)
  )
}
