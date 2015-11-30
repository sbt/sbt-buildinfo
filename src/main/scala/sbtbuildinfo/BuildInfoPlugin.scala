package sbtbuildinfo

import sbt._, Keys._
import java.io.File

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
    val BuildInfoType = sbtbuildinfo.BuildInfoType
    type BuildInfoType = sbtbuildinfo.BuildInfoType
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
    buildInfo := Seq(BuildInfo({
          if (buildInfoUsePackageAsPath.value)
            new File(sourceManaged.value, buildInfoPackage.value.split('.').mkString("/"))
          else
            sourceManaged.value / "sbt-buildinfo"
        },
        buildInfoRenderer.value,
        buildInfoObject.value,
        buildInfoKeys.value,
        buildInfoOptions.value,
        thisProjectRef.value,
        state.value,
        streams.value.cacheDirectory
    )),
    sourceGenerators ++= {
      if (buildInfoRenderer.value.isSource) Seq(buildInfo.taskValue) else Nil
    },
    resourceGenerators ++= {
      if(buildInfoRenderer.value.isResource) Seq(buildInfo.taskValue) else Nil
    },
    buildInfoRenderer := ScalaClassRenderer(
      buildInfoOptions.value,
      buildInfoPackage.value,
      buildInfoObject.value)
    )
  )

  def buildInfoDefaultSettings: Seq[Setting[_]] = Seq(
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoUsePackageAsPath := false,
    buildInfoKeys    := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber := buildNumberTask(baseDirectory.value, 1),
    buildInfoOptions := Seq()
  )
}
