package sbtbuildinfo

trait BuildInfoRenderer {

  def fileType: BuildInfoType
  def extension: String
  def header: Seq[String]
  def renderKeys(infoKeysNameAndValues: Seq[BuildInfoResult]): Seq[String]
  def footer: Seq[String]

  def isSource = fileType == BuildInfoType.Source
  def isResource = fileType == BuildInfoType.Resource
}
