package sbtbuildinfo

import sbt.*
import scala.reflect.ClassTag
import scala.quoted.*

object BuildInfoKey:
  import Entry.*

  def apply[A1: PluginCompat.Manifest](key: SettingKey[A1]): Entry[A1] =
    Entry.Setting(key)

  def apply[A1: PluginCompat.Manifest](key: TaskKey[A1]): Entry[A1] =
    Entry.Task(key)

  def apply[A1: PluginCompat.Manifest](tuple: (String, A1)): Entry[A1] =
    Entry.Constant(tuple)

  def map[A1, A2: PluginCompat.Manifest](from: Entry[A1])(fun: ((String, A1)) => (String, A2)): Entry[A2] =
    Entry.Mapped(from, fun)

  def action[A1: PluginCompat.Manifest](name: String)(fun: => A1): Entry[A1] =
    Entry.Action(name, () => fun)

  def outOfGraphUnsafe[A1: PluginCompat.Manifest](key: TaskKey[A1]): Entry[A1] =
    Entry.Task(key)
end BuildInfoKey
