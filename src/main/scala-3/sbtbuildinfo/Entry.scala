package sbtbuildinfo

import sbt.*
import PluginCompat.Manifest

enum Entry[A1: Manifest]:
  type A = A1
  def manifest: Manifest[A1] = Manifest[A1]

  case Setting[A1: Manifest](scoped: SettingKey[A1]) extends Entry[A1]
  case Task[A1: Manifest](scoped: TaskKey[A1]) extends Entry[A1]
  case TaskValue[A1: Manifest](task: sbt.Task[A1]) extends Entry[A1]
  case Constant[A1: Manifest](tuple: (String, A1)) extends Entry[A1]
  case Mapped[A1, A2: Manifest](from: Entry[A1], fun: ((String, A1)) => (String, A2)) extends Entry[A2]
  case Action[A1: Manifest](name: String, fun: () => A1) extends Entry[A1]
end Entry
