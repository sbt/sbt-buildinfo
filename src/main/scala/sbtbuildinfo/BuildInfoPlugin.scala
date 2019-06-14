package sbtbuildinfo

import sbt._, Keys._
import java.io.File
import BuildInfoType._

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
    buildInfo := {

      (
        buildInfoRenderers,
        sourceManaged,
        resourceManaged,
        buildInfoUsePackageAsPath,
        buildInfoObject,
        buildInfoKeys,
        buildInfoOptions,
        thisProjectRef,
        state,
        streams,
      ) flatMap { (
                    renderers: Set[BuildInfoRenderer],
                    srcDir: File,
                    resDir: File,
                    usePackageAsPath: Boolean,
                    obj: String,
                    keys: Seq[BuildInfoKey],
                    opts: Seq[BuildInfoOption],
                    pr: ProjectRef,
                    s: State,
                    taskStreams: TaskStreams,
                  ) =>

        renderers.toSeq.map { renderer =>
          val dir = {
            val parentDir = renderer.fileType match {
              case Source => srcDir
              case Resource => resDir
            }
            if (usePackageAsPath || renderers.size > 1)
              renderer.pkg match {
                case "" => parentDir
                case _ => parentDir / (renderer.pkg split '.' mkString "/")
              }
            else
              parentDir / "sbt-buildinfo"
          }

          BuildInfo(dir, renderer, obj, keys, opts, pr, s, taskStreams.cacheDirectory)
        }.join
      }
    }.value,
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
      buildInfoObject.value
    ),
    buildInfoRenderers := {
      buildInfoPackages.value.map { pkg =>
        buildInfoRenderFactory.value.apply(
          buildInfoOptions.value,
          pkg,
          buildInfoObject.value)
      } + buildInfoRenderer.value
    }
  ))

  def buildInfoDefaultSettings: Seq[Setting[_]] = Seq(
    buildInfoObject  := "BuildInfo",
    buildInfoPackage := "buildinfo",
    buildInfoPackages := Set(buildInfoPackage.value),
    buildInfoUsePackageAsPath := false,
    buildInfoKeys    := Seq(name, version, scalaVersion, sbtVersion),
    buildInfoBuildNumber := buildNumberTask(baseDirectory.value, 1),
    buildInfoOptions := Seq(),
    buildInfoRenderFactory := ScalaCaseObjectRenderer.apply
  )
}
