package sbtbuildinfo

import sbt._

object Plugin extends sbt.Plugin {
  import Keys._

  lazy val buildInfo        = TaskKey[Seq[File]]("buildinfo")
  lazy val buildInfoObject  = SettingKey[String]("buildinfo-object")
  lazy val buildInfoPackage = SettingKey[String]("buildinfo-package") 
  lazy val buildInfoKeys    = SettingKey[Seq[BuildInfo[_]]]("buildinfo-keys")
  lazy val buildInfoBuildNumber = TaskKey[Int]("buildinfo-buildnumber")

  object BuildInfo {
//    implicit def input[A](key: InputKey[A]): BuildInfo[A] = InputTaskInfo(key)
    implicit def setting[A](key: SettingKey[A]): BuildInfo[A] = SettingTaskInfo(key)

//    private final case class InputTaskInfo[A](scoped: InputKey[A]) extends ScopedLike[A] {
//      def entry(proj: ProjectRef, extracted: Extracted) = {
//        val (_, x) = extracted runTask (scoped in scope(proj), state)
//        Some(ident -> x.asInstanceOf[A])
//      }
//    }
    private final case class SettingTaskInfo[A](scoped: SettingKey[A]) extends ScopedLike[A] {
      def entry(proj: ProjectRef, extracted: Extracted) = {
        val valOpt = extracted getOpt (scoped in scope(proj))
        valOpt.map(ident -> _)
      }

//      val scope0 = info.scoped.scope
//      val scope = if (scope0 == This) scope0 in (proj) else scope0
//      info match {
//        case key: SettingKey[_] => extracted getOpt (key in scope)
//        case key: TaskKey[_]    =>
//          val (_, x) = extracted runTask (key in scope, state)
//          Some(x)
//        case _ => None
//      }
    }

    private final class Mapped[A, B](from: BuildInfo[A], fun: ((String, A)) => (String, B))
    extends Basic[B] {
      def scoped = from.scoped
      def entry(proj: ProjectRef, extracted: Extracted) = from.entry(proj, extracted) map fun
    }

    private trait Basic[A] extends BuildInfo[A] {
      def map[B](fun: ((String, A)) => (String, B)): BuildInfo[B] = new Mapped(this, fun)
    }

    private trait ScopedLike[A] extends Basic[A] {
      def scope(proj: ProjectRef) = {
        val scope0 = scoped.scope
        if (scope0 == This) scope0 in (proj) else scope0
      }

      def ident : String = {
        val scope = scoped.scope
        (scope.config.toOption match {
          case None => ""
          case Some(ConfigKey("compile")) => ""
          case Some(ConfigKey(x)) => x + "_"
        }) +
        (scope.task.toOption match {
          case None => ""
          case Some(x) => x.label + "_"
        }) +
        (scoped.key.label.split("-").toList match {
          case Nil => ""
          case x :: xs => x + (xs map {_.capitalize}).mkString("")
        })
      }
    }
  }
  sealed trait BuildInfo[A] {
    def scoped: Scoped
    def entry(proj: ProjectRef, extracted: Extracted): Option[(String, A)]
    def map[B](fun: ((String, A)) => (String, B)): BuildInfo[B]
  }

  private case class BuildInfoTask(dir: File, obj: String, pkg: String, keys: Seq[BuildInfo[_]],
    proj: ProjectRef, state: State) {
    private def extracted = Project.extract(state)

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

    private def line(info: BuildInfo[_]): Option[String] = info.entry(proj, extracted).map {
      case (ident, value) => "  val %s%s = %s" format
        (ident, getType(info) map { ": " + _ } getOrElse {""}, quote(value))
    }

    private def getType(info: BuildInfo[_]): Option[String] = {
      val key = info.scoped.key
      lazy val clazz = key.manifest.erasure
      lazy val firstType = key.manifest.typeArguments.headOption
      lazy val typeName =
        if(clazz == classOf[Task[_]]) firstType.toString
        else if(clazz == classOf[InputTask[_]]) firstType.toString
        else key.manifest.toString
      typeName match {
        case "scala.Option[java.lang.String]" => Some("Option[String]")
        case "scala.Option[Int]" => Some("Option[Int]")
        case "scala.Option[Double]" => Some("Option[Double]")
        case "scala.Option[Boolean]" => Some("Option[Boolean]")
        case "scala.Option[java.net.URL]" => Some("Option[java.net.URL]")
        case _ => None
      }
    }

    private def quote(v: Any): String = v match {
      case x: Int => x.toString
      case x: Long => x.toString + "L"
      case x: Double => x.toString
      case x: Boolean => x.toString
      case node: scala.xml.NodeSeq => node.toString
      case (k, _v) => "(%s -> %s)" format(quote(k), quote(_v))
      case mp: Map[_, _] => mp.toList.map(quote(_)).mkString("Map(", ", ", ")")
      case seq: Seq[_]   => seq.map(quote(_)).mkString("Seq(", ", ", ")")
      case op: Option[_] => op map { x => "Some(" + quote(x) + ")" } getOrElse {"None"}
      case url: java.net.URL => "new java.net.URL(\"%s\")" format url.toString
      case s => "\"%s\"" format s.toString
    }
  }

  private[this] def buildNumberTask(dir: File): Int = {
    val file: File = dir / "buildinfo.properties"
    val prop = new java.util.Properties

    def readProp: Int = {  
      prop.load(new java.io.FileInputStream(file))
      prop.getProperty("buildnumber", "0").toInt
    }
    def writeProp(value: Int) {
      prop.setProperty("buildnumber", value.toString)
      prop.store(new java.io.FileOutputStream(file), null)
    }
    val current = if (file.exists) readProp
                  else 0
    writeProp(current + 1)
    current
  }

  lazy val buildInfoSettings: Seq[Project.Setting[_]] = Seq(
    buildInfo <<= (sourceManaged in Compile,
        buildInfoObject, buildInfoPackage, buildInfoKeys, thisProjectRef, state) map {
      (dir, obj, pkg, keys, ref, state) =>
      Seq(BuildInfoTask(dir, obj, pkg, keys, ref, state).file)
    },
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoKeys    := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber <<= (baseDirectory) map { (dir) => buildNumberTask(dir) }
  )
}
