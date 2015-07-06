package sbtbuildinfo

sealed trait BuildInfoType
object BuildInfoType {
  case object Source extends BuildInfoType
  case object Resource extends BuildInfoType
}
