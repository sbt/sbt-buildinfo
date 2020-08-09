package hello

import scala.Predef._

object Test extends scala.App {
  val expected = """{"name":"helloworld", "scalaVersion":"2.12.12"}"""
  val actual = hello.BuildInfo.toJson
  assert(actual == expected, "expected " + expected + " but found " + actual)
}
