package sbtbuildinfo

object BuildInfoRenderer {
  type Factory = (Seq[BuildInfoOption], String) => BuildInfoRenderer
}

trait BuildInfoRenderer {

  def fileType: BuildInfoType
  def extension: String
  def renderKeys(packageName: String, infoKeysNameAndValues: Seq[BuildInfoResult]): Seq[String]

  def isSource = fileType == BuildInfoType.Source
  def isResource = fileType == BuildInfoType.Resource
}
