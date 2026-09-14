package sbtbuildinfo

import sbt.*
import scala.reflect.ClassTag
import scala.quoted.*

object BuildInfoKey:
  import Entry.*

  def apply[A1: PluginCompat.Manifest](key: SettingKey[A1]): Entry[A1] =
    Entry.Setting(key)

  // Expands to `key.taskValue`, which only works inside a setting macro
  // (`:=`, `+=`, ...): the macro makes the task a dependency of buildInfo, so
  // it runs once, as part of the build, instead of being run separately by the
  // plugin (#237). The `inline` parameter keeps the key expression itself in
  // the expansion, where the setting macro looks for it.
  inline def apply[A1: PluginCompat.Manifest](inline key: TaskKey[A1]): Entry[A1] =
    Entry.TaskValue(key.taskValue)

  def apply[A1: PluginCompat.Manifest](tuple: (String, A1)): Entry[A1] =
    Entry.Constant(tuple)

  def map[A1, A2: PluginCompat.Manifest](from: Entry[A1])(fun: ((String, A1)) => (String, A2)): Entry[A2] =
    Entry.Mapped(from, fun)

  def action[A1: PluginCompat.Manifest](name: String)(fun: => A1): Entry[A1] =
    Entry.Action(name, () => fun)

  def outOfGraphUnsafe[A1: PluginCompat.Manifest](key: TaskKey[A1]): Entry[A1] =
    Entry.Task(key)
end BuildInfoKey
