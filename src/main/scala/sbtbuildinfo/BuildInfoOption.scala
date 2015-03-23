package sbtbuildinfo

import sbt._, Keys._

sealed trait BuildInfoOption
object BuildInfoOption {
  case object ToMap extends BuildInfoOption
  case object ToJson extends BuildInfoOption
}
