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
    implicit def setting[A](key: SettingKey[A]): BuildInfo[A] = Setting(key)
    implicit def task[A](key: TaskKey[A]): BuildInfo[A] = Task(key)

    private[Plugin] final case class Setting[A](scoped: SettingKey[A]) extends Source[A] {
      def manifest = scoped.key.manifest
    }
    private[Plugin] final case class Task[A](scoped: TaskKey[A]) extends Source[A] {
      def manifest = scoped.key.manifest.typeArguments.head.asInstanceOf[Manifest[A]]
    }

    private[Plugin] final case class Mapped[A, B](from: BuildInfo[A], fun: ((String, A)) => (String, B))
                                                 (implicit val manifest: Manifest[B])
    extends BuildInfo[B]

    private[Plugin] sealed trait Source[A] extends BuildInfo[A]
  }
  sealed trait BuildInfo[A] {
    private[Plugin] def manifest: Manifest[A]
    final def mapInfo[B: Manifest](fun: ((String, A)) => (String, B)): BuildInfo[B] = BuildInfo.Mapped(this, fun)
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

    private def line(info: BuildInfo[_]): Option[String] = entry(info).map {
      case (ident, value) => "  val %s%s = %s" format
        (ident, getType(info) map { ": " + _ } getOrElse {""}, quote(value))
    }

    private def entry[A](info: BuildInfo[A]): Option[(String, A)] = info match {
      case BuildInfo.Setting(key)       => extracted getOpt (key in scope(key)) map { ident(key) -> _ }
      case BuildInfo.Task(key)          => Some( ident(key) -> extracted.runTask(key in scope(key), state)._2 )
      case BuildInfo.Mapped(from, fun)  => entry(from) map fun
    }

    private def scope(scoped: Scoped) = {
      val scope0 = scoped.scope
      if (scope0 == This) scope0 in (proj) else scope0
    }

    private def ident(scoped: Scoped) : String = {
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

    private def getType(info: BuildInfo[_]): Option[String] = {
      val mf = info.manifest
      if(mf.erasure == classOf[Option[_]]) {
        val s = mf.toString
        Some(if( s.startsWith("scala.")) s.substring(6) else s)
      } else None
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
