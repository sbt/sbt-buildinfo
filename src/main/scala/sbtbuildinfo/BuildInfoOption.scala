package sbtbuildinfo

import sbt._, Keys._

sealed trait BuildInfoOption
object BuildInfoOption {
  case object ToMap extends BuildInfoOption
  case object ToJson extends BuildInfoOption
  case class Traits(names: String*) extends BuildInfoOption
  case object BuildTime extends BuildInfoOption
}
