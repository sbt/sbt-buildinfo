package sbtbuildinfo

import sbt.*

// m: String is a hand-rolled TypeTag using macro.
enum Entry[A1]:
  type A = A1
  def manifest: String = this match
    case Setting(_, m)   => m
    case Task(_, m)      => m
    case Constant(_, m)  => m
    case Mapped(_, _, m) => m
    case Action(_, _, m) => m
    case _               => "???"
  
  case Setting[A1](scoped: SettingKey[A1], m: String) extends Entry[A1]
  case Task[A1](scoped: TaskKey[A1], m: String) extends Entry[A1]
  case TaskValue[A1](task: sbt.Task[A1]) extends Entry[A1]
  case Constant[A1](tuple: (String, A1), m: String) extends Entry[A1]
  case Mapped[A1, A2](from: Entry[A1], fun: ((String, A1)) => (String, A2), m: String) extends Entry[A2]
  case Action[A1](name: String, fun: () => A1, m: String) extends Entry[A1]
end Entry
