package hello

import scala.Predef._

object Test {
  def main(args: scala.Array[String]): scala.Unit = {
    val expected = """{"name":"helloworld", "scalaVersion":"2.12.21"}"""
    val actual = hello.BuildInfo.toJson
    assert(actual == expected, "expected " + expected + " but found " + actual)
  }
}
