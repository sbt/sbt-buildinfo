object StableState {
  // If you put one of these val's in build.sbt
  // each setting will get it's own AtomicInteger... -.-
  val counterOutOfTaskGraph = new java.util.concurrent.atomic.AtomicInteger()

  val counterInTaskGraph = new java.util.concurrent.atomic.AtomicInteger()
}
