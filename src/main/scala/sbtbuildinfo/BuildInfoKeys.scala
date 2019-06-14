package sbtbuildinfo

import sbt._

trait BuildInfoKeys {
  lazy val buildInfo            = taskKey[Seq[File]]("The task that generates the build info.")
  lazy val buildInfoRenderer    = settingKey[BuildInfoRenderer]("The renderer to use to generate the build info.")
  lazy val buildInfoRenderFactory = settingKey[BuildInfoRenderer.Factory]("The renderFactory to used to build the renderer.")
  lazy val buildInfoObject      = settingKey[String]("The name for the generated object.")
  lazy val buildInfoPackage     = settingKey[String]("The name for the generated package.")
  lazy val buildInfoPackages    = settingKey[Set[String]]("The names for the generated packages.")
  lazy val buildInfoUsePackageAsPath = settingKey[Boolean]("If true, the generated object is placed in the folder of the package instead of \"sbt-buildinfo\".")
  lazy val buildInfoKeys        = settingKey[Seq[BuildInfoKey.Entry[_]]]("Entries for build info.")
  lazy val buildInfoBuildNumber = taskKey[Int]("The build number.")
  lazy val buildInfoOptions     = settingKey[Seq[BuildInfoOption]]("Options for generating the build info.")
}

object BuildInfoKeys extends BuildInfoKeys
