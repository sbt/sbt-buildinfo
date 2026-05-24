package sbtbuildinfo

import sbt._, Keys._
import java.io.File
import PluginCompat.*
import sbt.Plugins.Basic
import sbt.util.CacheImplicits.{ *, given }

object BuildInfoPlugin extends AutoPlugin {
  type BuildInfoKey = PluginCompat.Entry[?]

  override def requires = plugins.JvmPlugin

  object autoImport extends BuildInfoKeys with PluginCompat.BuildInfoKeys0 {
    val BuildInfoKey = sbtbuildinfo.BuildInfoKey
    type BuildInfoKey = Entry[?]
    val BuildInfoOption = sbtbuildinfo.BuildInfoOption
    type BuildInfoOption = sbtbuildinfo.BuildInfoOption
    val BuildInfoType = sbtbuildinfo.BuildInfoType
    type BuildInfoType = sbtbuildinfo.BuildInfoType

    val addBuildInfoToConfig = buildInfoScopedSettings _

    val buildInfoValues: TaskKey[Seq[BuildInfoResult]] =
      taskKey("BuildInfo keys/values/types for use in the sbt build")
  }

  import autoImport.{ given, * }

  override def globalSettings: Seq[Def.Setting[?]] = Seq()

  def buildInfoDefaultSettings: Seq[Def.Setting[?]] = Seq(
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoUsePackageAsPath := false,
    buildInfoOptions := Seq(),
    buildInfoKeys := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber := buildNumberTask(baseDirectory.value, 1),
    buildInfoRenderFactory := (if(scalaVersion.value.startsWith("3")) Scala3CaseObjectRenderer.apply else ScalaCaseObjectRenderer.apply)
  )

  override def projectSettings: Seq[Def.Setting[?]] = buildInfoScopedSettings(Compile) ++ buildInfoDefaultSettings

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

  def buildInfoScopedSettings(conf: Configuration): Seq[Def.Setting[?]] = inConfig(conf)(Seq(
    buildInfo := Def.uncached((
        RichRichTaskable11((
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
        )).flatMapN { (
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
          BuildInfo(dir, renderer, obj, keys, opts, pr, s, taskStreams.cacheDirectory).map(Seq(_))
        }
    ).value),
    buildInfoValues := Def.uncached((
      RichRichTaskable4((buildInfoKeys, buildInfoOptions, thisProjectRef, state)).flatMapN ((keys, opts, pr, s) =>
        BuildInfo.results(keys, opts, pr, s)
      )
    ).value),

    sourceGenerators ++= (if (buildInfoRenderer.value.isSource) Seq(buildInfo.taskValue) else Nil),
    resourceGenerators ++= (if (buildInfoRenderer.value.isResource) Seq(buildInfo.taskValue) else Nil),
    buildInfoRenderer := buildInfoRenderFactory.value.apply(
      buildInfoOptions.value,
      buildInfoPackage.value,
      buildInfoObject.value)
    )
  )
}
