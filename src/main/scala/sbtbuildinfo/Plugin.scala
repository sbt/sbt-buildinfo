package sbtbuildinfo

import sbt._

object Plugin extends sbt.Plugin {
  import Keys._

  lazy val buildInfo        = TaskKey[Seq[File]]("buildinfo")
  lazy val buildInfoObject  = SettingKey[String]("buildinfo-object")
  lazy val buildInfoPackage = SettingKey[String]("buildinfo-package") 
  lazy val buildInfoKeys    = SettingKey[Seq[BuildInfo]]("buildinfo-keys")
  lazy val buildInfoBuildNumber = TaskKey[Int]("buildinfo-buildnumber")

  object BuildInfo {
    implicit def scoped(s: Scoped): BuildInfo = ScopedImpl(s)

    private final case class ScopedImpl(scoped: Scoped) extends BuildInfo {
      def label = scoped.key.label
    }
  }
  sealed trait BuildInfo { def scoped: Scoped; def label: String }

  private case class BuildInfoTask(dir: File, obj: String, pkg: String, keys: Seq[BuildInfo],
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

    private def line(info: BuildInfo): Option[String] =
      value(info) map { x => "  val %s%s = %s" format (ident(info),
        getType(info) map { ": " + _ } getOrElse {""}, quote(x)) }

    private def value(info: BuildInfo): Option[Any] = {
      val scope0 = info.scoped.scope
      val scope = if (scope0 == This) scope0 in (proj) else scope0
      info match {
        case key: SettingKey[_] => extracted getOpt (key in scope)
        case key: TaskKey[_]    =>
          val (_, x) = extracted runTask (key in scope, state)
          Some(x)
        case _ => None
      }      
    }

    private def ident(info: BuildInfo): String = {
      val scope = info.scoped.scope
      (scope.config.toOption match {
        case None => ""
        case Some(ConfigKey("compile")) => ""
        case Some(ConfigKey(x)) => x + "_"
      }) +
      (scope.task.toOption match {
        case None => ""
        case Some(x) => x.label + "_"
      }) +
      (info.label.split("-").toList match {
        case Nil => ""
        case x :: xs => x + (xs map {_.capitalize}).mkString("")
      })
    }

    private def getType(info: BuildInfo): Option[String] = {
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
    buildInfoKeys    := Seq[BuildInfo](name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber <<= (baseDirectory) map { (dir) => buildNumberTask(dir) }
  )
}
