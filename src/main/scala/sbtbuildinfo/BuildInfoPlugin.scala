package sbtbuildinfo

import sbt._, Keys._

object BuildInfoPlugin extends sbt.AutoPlugin {
  type BuildInfoKey = BuildInfoKey.Entry[_]

  override def requires = plugins.JvmPlugin
  override def projectSettings: Seq[Def.Setting[_]] =
    buildInfoSettings

  object autoImport extends BuildInfoKeys {
    val BuildInfoKey = sbtbuildinfo.BuildInfoKey
    type BuildInfoKey = sbtbuildinfo.BuildInfoKey
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

  lazy val buildInfoSettings: Seq[Def.Setting[_]] = Seq(
    buildInfo <<= (sourceManaged in Compile,
        buildInfoObject, buildInfoPackage, buildInfoKeys, thisProjectRef, state, streams) map {
      (dir, obj, pkg, keys, ref, state, taskStreams) =>
      Seq(BuildInfo(dir / "sbt-buildinfo", obj, pkg, keys, ref, state, taskStreams.cacheDirectory))
    },
    sourceGenerators in Compile <+= buildInfo,
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoKeys    := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber <<= (baseDirectory) map { (dir) => buildNumberTask(dir, 1) }
  )
}
