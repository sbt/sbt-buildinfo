package sbtbuildinfo

import sbt._, Keys._

case class BuildInfoResult(identifier: String, value: Any, typeExpr: TypeExpression)

object BuildInfo {
  def apply(dir: File, renderer: BuildInfoRenderer, obj: String,
            keys: Seq[BuildInfoKey], options: Seq[BuildInfoOption],
            proj: ProjectRef, state: State, cacheDir: File): Task[File] =
    BuildInfoTask(dir, renderer, obj, keys, options, proj, state, cacheDir).file

  private def extraKeys(options: Seq[BuildInfoOption]): Seq[BuildInfoKey] =
      if (options contains BuildInfoOption.BuildTime) {
        val now = System.currentTimeMillis()
        val dtf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        dtf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
        val nowStr = dtf.format(new java.util.Date(now))
        Seq[BuildInfoKey](
          "builtAtString" -> nowStr,
          "builtAtMillis" -> now
        )
      } else {
        Seq.empty[BuildInfoKey]
      }

  def results(keys: Seq[BuildInfoKey], options: Seq[BuildInfoOption], project: ProjectRef, state: State): Task[Seq[BuildInfoResult]] = {
    val distinctKeys = (keys ++ extraKeys(options)).toList.distinct
    val extracted = Project.extract(state)

    def entry[A](info: BuildInfoKey.Entry[A]): Option[Task[BuildInfoResult]] = {
      val typeExpr = TypeExpression.parse(info.manifest.toString())._1
      val result = info match {
        case BuildInfoKey.Setting(key)      => extracted getOpt (key in scope(key, project)) map (v => task(ident(key) -> v))
        case BuildInfoKey.Task(key)         => Some(task(ident(key) -> extracted.runTask(key in scope(key, project), state)._2))
        case BuildInfoKey.TaskValue(task)   => Some(task.map(x => ident(task) -> x))
        case BuildInfoKey.Constant(tuple)   => Some(task(tuple))
        case BuildInfoKey.Action(name, fun) => Some(task(name -> fun.apply))
        case BuildInfoKey.Mapped(from, fun) => entry(from) map (_ map (r => fun((r.identifier, r.value.asInstanceOf[A]))))
      }
      result map (_ map { case (identifier, value) => BuildInfoResult(identifier, value, typeExpr) })
    }

    distinctKeys.flatMap(entry(_)).join
  }

  private def scope(scoped: Scoped, project: ProjectReference) = {
    val scope0 = scoped.scope
    if (scope0.project == This) scope0 in project
    else scope0
  }

  private def ident(scoped: Scoped): String = ident(scoped.scope, scoped.key)
  private def ident(scoped: ScopedKey[_]): String = ident(scoped.scope, scoped.key)

  private def ident(scope: Scope, attrKey: AttributeKey[_]): String = {
    val config = scope.config.toOption map (_.name) filter (_ != "compile")
    val inTask = scope.task.toOption map (_.label)
    val key = attrKey.label.split("-").toList match {
      case Nil     => ""
      case x :: xs => x + (xs map (_.capitalize) mkString "")
    }
    Seq(config, inTask, Some(key)).flatten mkString "_"
  }

  private def ident(task: Task[_]): String = (
    task.info.name
      orElse (task.info.attributes get taskDefinitionKey map ident)
      getOrElse s"<anon-${System identityHashCode task}>"
  )


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

    val tempFile = cacheDir / "sbt-buildinfo" / dir.toString / s"$obj.${renderer.extension}"
    val outFile = dir / s"$obj.${renderer.extension}"

    // 1. make the file under cache/sbtbuildinfo.
    // 2. compare its SHA1 against cache/sbtbuildinfo-inputs
    def file: Task[File] = {
      makeFile(tempFile) map { _ =>
        cachedCopyFile(hash(tempFile))
        outFile
      }
    }

    val cachedCopyFile =
      inputChanged(cacheDir / "sbtbuildinfo-inputs") { (inChanged, input: HashFileInfo) =>
        if (inChanged || !outFile.exists) {
          IO.copyFile(tempFile, outFile, preserveLastModified = true)
        } // if
      }

    def makeFile(file: File): Task[File] = {
      results(keys, options, proj, state) map { values =>
        val lines = renderer.renderKeys(values)
        IO.writeLines(file, lines, IO.utf8)
        file
      }
    }

  }

}
