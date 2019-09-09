package sbtbuildinfo

object BuildInfoRenderer {
  type Factory = (Seq[BuildInfoOption], String, String) => BuildInfoRenderer
}

trait BuildInfoRenderer {

  def options: Seq[BuildInfoOption]
  def fileType: BuildInfoType
  def extension: String
  def renderKeys(infoKeysNameAndValues: Seq[BuildInfoResult]): Seq[String]

  def isSource = fileType == BuildInfoType.Source
  def isResource = fileType == BuildInfoType.Resource
  def isPkgPriv: Boolean = options contains BuildInfoOption.PackagePrivate
}
