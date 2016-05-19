package sbtbuildinfo

import sbt._, syntax._, Keys._

case class BuildInfoResult(identifier: String, value: Any, typeExpr: TypeExpression)

object BuildInfo {
  def apply(dir: File, renderer: BuildInfoRenderer, obj: String,
            keys: Seq[BuildInfoKey], options: Seq[BuildInfoOption],
            proj: ProjectRef, state: State, cacheDir: File): File =
    BuildInfoTask(dir, renderer, obj, keys, options, proj, state, cacheDir).file

  private case class BuildInfoTask(dir: File,
                                   renderer: BuildInfoRenderer,
                                   obj: String,
                                   keys: Seq[BuildInfoKey],
                                   options: Seq[BuildInfoOption],
                                   proj: ProjectRef,
                                   state: State,
                                   cacheDir: File) {
    import FileInfo.hash
    import Tracked.inputChanged

    def extracted = Project.extract(state)
    val tempFile = cacheDir / "sbt-buildinfo" / s"$obj.${renderer.extension}"
    val outFile = dir / s"$obj.${renderer.extension}"

    // 1. make the file under cache/sbtbuildinfo.
    // 2. compare its SHA1 against cache/sbtbuildinfo-inputs
    def file: File = {
      makeFile(tempFile)
      cachedCopyFile(hash(tempFile))
      outFile
    }

    val cachedCopyFile =
      inputChanged(cacheDir / "sbtbuildinfo-inputs") { (inChanged, input: HashFileInfo) =>
        if (inChanged || !outFile.exists) {
          IO.copyFile(tempFile, outFile, preserveLastModified = true)
        } // if
      }

    def makeKeys : List[BuildInfoKey] = {
      val extraKeys = {
        if (options contains BuildInfoOption.BuildTime) {
          val now = System.currentTimeMillis()
          val dtf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
          dtf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
          val nowStr = dtf.format(new java.util.Date(now))
          Seq[BuildInfoKey] (
            "builtAtString" -> nowStr ,
            "builtAtMillis" -> now
          )
        } else {
          Seq.empty[BuildInfoKey]
        }
      }
      (keys ++ extraKeys).toList.distinct
    }

    def makeFile(file: File): File = {
      val distinctKeys = makeKeys
      val values = distinctKeys.flatMap(entry(_))
      val lines = renderer.header ++ renderer.renderKeys(values) ++ renderer.footer
      IO.writeLines(file, lines, IO.utf8)
      file
    }

    def entry[A](info: BuildInfoKey.Entry[A]): Option[BuildInfoResult] = {
      val typeExpr = TypeExpression.parse(info.manifest.toString())._1
      val result = info match {
        case BuildInfoKey.Setting(key)      => extracted getOpt (key in scope(key)) map { ident(key) -> _ }
        case BuildInfoKey.Task(key)         => Some(ident(key) -> extracted.runTask(key in scope(key), state)._2)
        case BuildInfoKey.Constant(tuple)   => Some(tuple)
        case BuildInfoKey.Action(name, fun) => Some(name -> fun.apply)
        case BuildInfoKey.Mapped(from, fun) => entry(from).map { r => fun(r.identifier -> r.value.asInstanceOf[A]) }
      }
      result.map(r => BuildInfoResult(r._1, r._2, typeExpr))
    }

    def scope(scoped: Scoped) = {
      val scope0 = scoped.scope
      if (scope0.project == This) scope0 in proj
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
  }
}
