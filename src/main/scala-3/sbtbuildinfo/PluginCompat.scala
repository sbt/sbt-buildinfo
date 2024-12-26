package sbtbuildinfo

import com.eed3si9n.manifesto.Manifesto
import java.nio.file.{ Path => NioPath }
import sbt.*
import scala.annotation.nowarn
import xsbti.{ FileConverter, HashedVirtualFileRef, VirtualFile }

object PluginCompat:
  type FileRef = HashedVirtualFileRef
  type Out = VirtualFile
  type Entry[A1] = sbtbuildinfo.Entry[A1]
  type Manifest[A1] = Manifesto[A1]
  val Manifest = Manifesto

  val Setting = Entry.Setting
  val Task = Entry.Task
  val TaskValue = Entry.TaskValue
  val Constant = Entry.Constant
  val Action = Entry.Action
  val Mapped = Entry.Mapped

  def toClasspath(cp: Vector[NioPath])(using conv: FileConverter): Seq[Attributed[HashedVirtualFileRef]] =
    cp.map((x) => Attributed.blank(conv.toVirtualFile(x)))

  def taskName(task: Task[?]): Option[String] = task.name
  def taskAttributes(task: Task[?]) = task.attributes

  trait BuildInfoKeys0:
    @nowarn inline given [A1]: Conversion[SettingKey[A1], Entry[A1]] = BuildInfoKey(_)
    @nowarn inline given [A1]: Conversion[TaskKey[A1], Entry[A1]] = BuildInfoKey(_)
    @nowarn inline given [A1]: Conversion[sbt.Task[A1], Entry[A1]] = Entry.TaskValue[A1](_)
    @nowarn inline given [A1]: Conversion[(String, A1), Entry[A1]] = BuildInfoKey(_)
  end BuildInfoKeys0

  object TypeExpression:
    def unapply(m: Manifest[?]): (String, List[Manifest[?]]) =
      (m.typeCon, m.typeArguments)
  end TypeExpression

  object BuildInfoKeys0 extends BuildInfoKeys0

  inline def RichRichTaskable4[A1, A2, A3, A4](tuple: (A1, A2, A3, A4)) = tuple
  inline def RichRichTaskable11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](tuple: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)) =
    tuple
end PluginCompat
