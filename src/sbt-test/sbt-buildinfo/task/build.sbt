import StableState.{ counterOutOfTaskGraph, counterInTaskGraph }

scalaVersion in ThisBuild := "2.12.4"

val projOutOfTaskGraph1 = project settings (
  sourceGenerators in Compile += Def.task { counterOutOfTaskGraph.incrementAndGet(); Nil }.taskValue
)

val projOutOfTaskGraph2 = project dependsOn projOutOfTaskGraph1 settings (
  BuildInfoPlugin.buildInfoDefaultSettings,
  addBuildInfoToConfig(Test),
  buildInfoKeys in Test += BuildInfoKey.outOfGraphUnsafe(fullClasspath in Compile),
)

val projInTaskGraph1 = project settings (
  sourceGenerators in Compile += Def.task { counterInTaskGraph.incrementAndGet(); Nil }.taskValue
)

val projInTaskGraph2 = project dependsOn projInTaskGraph1 settings (
  BuildInfoPlugin.buildInfoDefaultSettings,
  addBuildInfoToConfig(Test),
  buildInfoKeys in Test += BuildInfoKey.of(fullClasspath in Compile)
)

TaskKey[Unit]("checkOutOfTaskGraph") := {
  val value = counterOutOfTaskGraph.get()
  assert(value == 2, s"Expected counterOutOfTaskGraph == 2, was $value")
}

TaskKey[Unit]("checkInTaskGraph") := {
  val value = counterInTaskGraph.get()
  assert(value == 1, s"Expected counterInTaskGraph == 1, was $value")
}
