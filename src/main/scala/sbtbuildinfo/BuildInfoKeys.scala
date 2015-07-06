package sbtbuildinfo

import sbt._
import Keys._

trait BuildInfoKeys {
  lazy val buildInfo        = taskKey[Seq[File]]("Generates build info.")
  lazy val buildInfoRenderer = settingKey[BuildInfoRenderer]("The renderer used to create the build info file")
  lazy val buildInfoObject  = settingKey[String]("The name for the generated object.")
  lazy val buildInfoPackage = settingKey[String]("The name for teh generated package.") 
  lazy val buildInfoKeys    = settingKey[Seq[BuildInfoKey.Entry[_]]]("Entries for build info.")
  lazy val buildInfoBuildNumber = taskKey[Int]("The build number.")
  lazy val buildInfoOptions = settingKey[Seq[BuildInfoOption]]("Options to generate build info.")
}
object BuildInfoKeys extends BuildInfoKeys
