package sbtbuildinfo

import sbt._

object Plugin extends sbt.Plugin {
  import Keys._

  lazy val buildInfo        = TaskKey[Seq[File]]("buildinfo")
  lazy val buildInfoObject  = SettingKey[String]("buildinfo-object")
  lazy val buildInfoPackage = SettingKey[String]("buildinfo-package") 
  lazy val buildInfoKeys    = SettingKey[Seq[BuildInfoKey.Entry[_]]]("buildinfo-keys")
  lazy val buildInfoBuildNumber = TaskKey[Int]("buildinfo-buildnumber")

  object BuildInfoKey {
    implicit def setting[A](key: SettingKey[A]): Entry[A] = Setting(key)
    implicit def task[A](key: TaskKey[A]): Entry[A] = Task(key)
    implicit def constant[A: Manifest](tuple: (String, A)): Entry[A] = Constant(tuple)
    
    def apply[A](key: SettingKey[A]): Entry[A] = Setting(key)
    def apply[A](key: TaskKey[A]): Entry[A] = Task(key)
    def apply[A: Manifest](tuple: (String, A)): Entry[A] = Constant(tuple)
    def map[A, B: Manifest](from: Entry[A])(fun: ((String, A)) => (String, B)): Entry[B] =
      BuildInfoKey.Mapped(from, fun)
    def action[A: Manifest](name: String)(fun: => A): Entry[A] = Action(name, () => fun)

    private[Plugin] final case class Setting[A](scoped: SettingKey[A]) extends Entry[A] {
      def manifest = scoped.key.manifest
    }
    private[Plugin] final case class Task[A](scoped: TaskKey[A]) extends Entry[A] {
      def manifest = scoped.key.manifest.typeArguments.head.asInstanceOf[Manifest[A]]
    }

    private[Plugin] final case class Constant[A](tuple: (String, A))(implicit val manifest: Manifest[A])
    extends Entry[A]

    private[Plugin] final case class Mapped[A, B](from: Entry[A], fun: ((String, A)) => (String, B))
                                                 (implicit val manifest: Manifest[B])
    extends Entry[B]

    private[Plugin] final case class Action[A](name: String, fun: () => A)(implicit val manifest: Manifest[A])
    extends Entry[A]

    sealed trait Entry[A] {
      private[Plugin] def manifest: Manifest[A]
    }
  }

  type BuildInfoKey = BuildInfoKey.Entry[_]

  object BuildInfo {
    def apply(dir: File, obj: String, pkg: String, keys: Seq[BuildInfoKey],
        proj: ProjectRef, state: State, cacheDir: File): File =
      BuildInfoTask(dir, obj, pkg, keys, proj, state, cacheDir).file

    private case class BuildInfoTask(dir: File, obj: String, pkg: String, keys: Seq[BuildInfoKey],
        proj: ProjectRef, state: State, cacheDir: File) {
      import FileInfo.hash
      import Tracked.inputChanged

      def extracted = Project.extract(state)
      val tempFile = cacheDir / "sbtbuildinfo" / ("%s.scala" format obj)
      val outFile = dir / ("%s.scala" format obj)

      // 1. make the file under cache/sbtbuildinfo.
      // 2. compare its SHA1 against cache/sbtbuildinfo-inputs
      def file: File = {
        makeFile(tempFile)
        cachedCopyFile { hash(tempFile) }
        outFile
      }

      val cachedCopyFile =
        inputChanged(cacheDir / "sbtbuildinfo-inputs") { (inChanged, input: HashFileInfo) =>
          if (inChanged || !outFile.exists) {
            IO.copyFile(tempFile, outFile, true)
          } // if
        }

      def makeFile(file: File): File = {
        val lines =
          List("package %s" format pkg,
            "",
            "case object %s {" format obj) :::
          (keys.toList.distinct map { line(_) }).flatten :::
          List("}")
        IO.write(file, lines.mkString("\n"))
        file
      }

      def line(info: BuildInfoKey): Option[String] = entry(info).map {
        case (ident, value) => "  val %s%s = %s" format
          (ident, getType(info) map { ": " + _ } getOrElse {""}, quote(value))
      }

      def entry[A](info: BuildInfoKey.Entry[A]): Option[(String, A)] = info match {
        case BuildInfoKey.Setting(key)      => extracted getOpt (key in scope(key)) map { ident(key) -> _ }
        case BuildInfoKey.Task(key)         => Some(ident(key) -> extracted.runTask(key in scope(key), state)._2)
        case BuildInfoKey.Constant(tuple)   => Some(tuple)
        case BuildInfoKey.Action(name, fun) => Some(name -> fun.apply)
        case BuildInfoKey.Mapped(from, fun) => entry(from) map fun
      }

      def scope(scoped: Scoped) = {
        val scope0 = scoped.scope
        if (scope0.project == This) scope0 in (proj)
        else scope0
      }

      def ident(scoped: Scoped) : String = {
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

      def getType(info: BuildInfoKey): Option[String] = {
        val mf = info.manifest
        if(mf.erasure == classOf[Option[_]]) {
          val s = mf.toString
          Some(if( s.startsWith("scala.")) s.substring(6) else s)
        } else None
      }

      def quote(v: Any): String = v match {
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
  }

  def buildNumberTask(dir: File, increment: Int): Int = {
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
    writeProp(current + increment)
    current
  }

  lazy val buildInfoSettings: Seq[Project.Setting[_]] = Seq(
    buildInfo <<= (sourceManaged in Compile,
        buildInfoObject, buildInfoPackage, buildInfoKeys, thisProjectRef, state, cacheDirectory) map {
      (dir, obj, pkg, keys, ref, state, cacheDir) =>
      Seq(BuildInfo(dir / "sbt-buildinfo", obj, pkg, keys, ref, state, cacheDir))
    },
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoKeys    := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber <<= (baseDirectory) map { (dir) => buildNumberTask(dir, 1) }
  )
}
