package sbtbuildinfo

import java.nio.file.{ Path => NioPath }
import sbt.*
import scala.language.higherKinds
import scala.annotation.nowarn

object PluginCompat {
  type FileRef = java.io.File
  type Out = java.io.File
  type Entry[A1] = sbtbuildinfo.BuildInfoKey.Entry[A1]

  val Setting = BuildInfoKey.Setting
  val Task = BuildInfoKey.Task
  val TaskValue = BuildInfoKey.TaskValue
  val Constant = BuildInfoKey.Constant
  val Mapped = BuildInfoKey.Mapped
  val Action = BuildInfoKey.Action

  trait BuildInfoKeys0
  object BuildInfoKeys0 extends BuildInfoKeys0

  def toClasspath(cp: Vector[NioPath]): Seq[Attributed[File]] =
    cp.map((x) => Attributed.blank(x.toFile()))

  implicit class RichScope(scope: Scope) {
    @nowarn
    def rescope(ref: Reference): Scope = scope.in(ref)
  }
  implicit class RichRichTaskable4[A1, A2, A3, A4](
    val taskable: Scoped.RichTaskable4[A1, A2, A3, A4]) {
    def flatMapN[R](f: (A1, A2, A3, A4) => Task[R]) =
      taskable.flatMap(f)
  }
  implicit class RichRichTaskable11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](
    val taskable: Scoped.RichTaskable11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11]) {
    def flatMapN[R](f: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => Task[R]) =
      taskable.flatMap(f)
  }
}
