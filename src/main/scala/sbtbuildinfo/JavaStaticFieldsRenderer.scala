package sbtbuildinfo

case class JavaStaticFieldsRenderer(options: Seq[BuildInfoOption], pkg: String, cl: String) extends JavaRenderer(pkg, cl, true) {

  override def renderKeys(buildInfoResults: Seq[BuildInfoResult]): Seq[String] =
    header ++
      buildInfoResults.flatMap(line) ++
      Seq(toStringLines(buildInfoResults)) ++
      toMapLines(buildInfoResults) ++
      Seq(buildUrlLines, buildMapLines) ++
      buildJsonLines ++
      toJsonLines ++
      footer

}
