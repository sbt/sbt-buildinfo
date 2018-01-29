import sbt._

package object sbtbuildinfo {
  type BuildInfoKey = BuildInfoKey.Entry[_]
  object BuildInfoKey {
    implicit def setting[A](key: SettingKey[A]): Entry[A] = Setting(key)
    implicit def task[A](key: TaskKey[A]): Entry[A] = Task(key)
    implicit def taskValue[A: Manifest](task: sbt.Task[A]): Entry[A] = TaskValue(task)
    implicit def constant[A: Manifest](tuple: (String, A)): Entry[A] = Constant(tuple)
    
    def apply[A](key: SettingKey[A]): Entry[A] = Setting(key)
    def apply[A](key: TaskKey[A]): Entry[A] = Task(key)
    def apply[A: Manifest](tuple: (String, A)): Entry[A] = Constant(tuple)
    def map[A, B: Manifest](from: Entry[A])(fun: ((String, A)) => (String, B)): Entry[B] =
      BuildInfoKey.Mapped(from, fun)
    def action[A: Manifest](name: String)(fun: => A): Entry[A] = Action(name, () => fun)

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
