package sbtbuildinfo

import sbt._, Keys._
import java.io.File

object BuildInfoPlugin extends AutoPlugin {
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

    val buildInfoValues: TaskKey[Seq[BuildInfoResult]] =
      taskKey("BuildInfo keys/values/types for use in the sbt build")
  }
  import autoImport._

  def buildNumberTask(dir: File, increment: Int): Int = {
    val file: File = dir / "buildinfo.properties"
    val prop = new java.util.Properties

    def readProp: Int = {
      prop.load(new java.io.FileInputStream(file))
      prop.getProperty("buildnumber", "0").toInt
    }
    def writeProp(value: Int) = {
      prop.setProperty("buildnumber", value.toString)
      prop.store(new java.io.FileOutputStream(file), null)
    }
    val current = if (file.exists) readProp
                  else 0
    writeProp(current + increment)
    current
  }

  import TupleSyntax._

  def buildInfoScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(Seq(
    buildInfo := (
        (
          buildInfoRenderer,
          sourceManaged,
          resourceManaged,
          buildInfoUsePackageAsPath,
          buildInfoPackage,
          buildInfoObject,
          buildInfoKeys,
          buildInfoOptions,
          thisProjectRef,
          state,
          streams,
        ) flatMap { (
            renderer: BuildInfoRenderer,
            srcDir: File,
            resDir: File,
            usePackageAsPath: Boolean,
            packageName: String,
            obj: String,
            keys: Seq[BuildInfoKey],
            opts: Seq[BuildInfoOption],
            pr: ProjectRef,
            s: State,
            taskStreams: TaskStreams,
        ) =>
          val dir = {
            val parentDir = renderer.fileType match {
              case BuildInfoType.Source   => srcDir
              case BuildInfoType.Resource => resDir
            }
            if (usePackageAsPath)
              packageName match {
                case "" => parentDir
                case _  => parentDir / (packageName split '.' mkString "/")
              }
            else
              parentDir / "sbt-buildinfo"
          }
          BuildInfo(dir, renderer, obj, keys, opts, pr, s, taskStreams.cacheDirectory) map (Seq(_))
        }
    ).value,
    buildInfoValues := (
      (buildInfoKeys, buildInfoOptions, thisProjectRef, state) flatMap ((keys, opts, pr, s) =>
        BuildInfo.results(keys, opts, pr, s)
      )
    ).value,

    sourceGenerators ++= (if (buildInfoRenderer.value.isSource) Seq(buildInfo.taskValue) else Nil),
    resourceGenerators ++= (if (buildInfoRenderer.value.isResource) Seq(buildInfo.taskValue) else Nil),
    buildInfoRenderer := buildInfoRenderFactory.value.apply(
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
    buildInfoOptions := Seq(),
    buildInfoRenderFactory := (if(scalaVersion.value.startsWith("3")) Scala3CaseObjectRenderer.apply else ScalaCaseObjectRenderer.apply)
  )
}
