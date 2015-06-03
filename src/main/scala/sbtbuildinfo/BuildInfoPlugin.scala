package sbtbuildinfo

import sbt._, Keys._

object BuildInfoPlugin extends sbt.AutoPlugin {
  type BuildInfoKey = BuildInfoKey.Entry[_]

  override def requires = plugins.JvmPlugin
  override def projectSettings: Seq[Def.Setting[_]] =
    buildInfoScopedSettings(Compile) ++ buildInfoDefaultSettings

  object autoImport extends BuildInfoKeys {
    val BuildInfoKey = sbtbuildinfo.BuildInfoKey
    type BuildInfoKey = sbtbuildinfo.BuildInfoKey
    val BuildInfoOption = sbtbuildinfo.BuildInfoOption
    type BuildInfoOption = sbtbuildinfo.BuildInfoOption
    val addBuildInfoToConfig = buildInfoScopedSettings _
  }
  import autoImport._

  def buildNumberTask(dir: File, increment: Int): Int = {
    val file: File = dir / "buildinfo.properties"
    val prop = new java.util.Properties

    def readProp: Int = {  
      prop.load(new java.io.FileInputStream(file))
      prop.getProperty("buildnumber", "0").toInt
    }
    def writeProp(value: Int) {
      prop.setProperty("buildnumber", value.toString)
      prop.store(new java.io.FileOutputStream(file), null)
    }
    val current = if (file.exists) readProp
                  else 0
    writeProp(current + increment)
    current
  }

  def buildInfoScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(Seq(
    buildInfo := {
      val dir = sourceManaged.value
      Seq(BuildInfo(dir / "sbt-buildinfo",
        buildInfoObject.value,
        buildInfoPackage.value,
        buildInfoKeys.value,
        buildInfoOptions.value,
        thisProjectRef.value,
        state.value,
        streams.value.cacheDirectory))
    },
    sourceGenerators <+= buildInfo
  ))

  def buildInfoDefaultSettings: Seq[Setting[_]] = Seq(
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoKeys    := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber <<= (baseDirectory) map { (dir) => buildNumberTask(dir, 1) },
    buildInfoOptions := Seq()
  )
}
