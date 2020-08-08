import sbt._

package object sbtbuildinfo {
  type BuildInfoKey = BuildInfoKey.Entry[_]
  object BuildInfoKey {
    implicit def sbtbuildinfoSettingEntry[A](key: SettingKey[A]): Entry[A] = Setting(key)
    implicit def sbtbuildinfoTaskEntry[A](key: TaskKey[A]): Entry[A] = macro BuildInfoKeyMacros.taskImpl
    implicit def sbtbuildinfoTaskValueEntry[A: Manifest](task: sbt.Task[A]): Entry[A] = TaskValue(task)
    implicit def sbtbuildinfoConstantEntry[A: Manifest](tuple: (String, A)): Entry[A] = Constant(tuple)

    def apply[A](key: SettingKey[A]): Entry[A] = Setting(key)
    def apply[A](key: TaskKey[A]): Entry[A] = macro BuildInfoKeyMacros.taskImpl
    def apply[A: Manifest](tuple: (String, A)): Entry[A] = Constant(tuple)
    def map[A, B: Manifest](from: Entry[A])(fun: ((String, A)) => (String, B)): Entry[B] =
      BuildInfoKey.Mapped(from, fun)
    def action[A: Manifest](name: String)(fun: => A): Entry[A] = Action(name, () => fun)

    @deprecated("use += (x: BuildInfoKey) instead", "0.10.0")
    def of[A](x: BuildInfoKey.Entry[A]): BuildInfoKey.Entry[A] = x
    @deprecated("use ++= Seq[BuildInfoKey](...) instead", "0.10.0")
    def ofN(xs: BuildInfoKey*): Seq[BuildInfoKey] = xs

    def outOfGraphUnsafe[A](key: TaskKey[A]): Entry[A] = Task(key)

    private[sbtbuildinfo] final case class Setting[A](scoped: SettingKey[A]) extends Entry[A] {
      def manifest = scoped.key.manifest
    }
    private[sbtbuildinfo] final case class Task[A](scoped: TaskKey[A]) extends Entry[A] {
      def manifest = scoped.key.manifest.typeArguments.head.asInstanceOf[Manifest[A]]
    }

    private[sbtbuildinfo] final case class TaskValue[A](task: sbt.Task[A])(implicit val manifest: Manifest[A])
    extends Entry[A]

    private[sbtbuildinfo] final case class Constant[A](tuple: (String, A))(implicit val manifest: Manifest[A])
    extends Entry[A]

    private[sbtbuildinfo] final case class Mapped[A, B](from: Entry[A], fun: ((String, A)) => (String, B))
                                                 (implicit val manifest: Manifest[B])
    extends Entry[B]

    private[sbtbuildinfo] final case class Action[A](name: String, fun: () => A)(implicit val manifest: Manifest[A])
    extends Entry[A]

    sealed trait Entry[A] {
      private[sbtbuildinfo] def manifest: Manifest[A]
    }
  }
}
