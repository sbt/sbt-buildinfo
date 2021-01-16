package sbtbuildinfo

case class JavaSingletonRenderer(options: Seq[BuildInfoOption], pkg: String, cl: String) extends JavaRenderer(pkg, cl, false) {

  override def renderKeys(buildInfoResults: Seq[BuildInfoResult]): Seq[String] =
    header ++
      instanceLine ++
      buildInfoResults.flatMap(line) ++
      Seq(toStringLines(buildInfoResults)) ++
      toMapLines(buildInfoResults) ++
      Seq(buildUrlLines, buildMapLines) ++
      buildJsonLines ++
      toJsonLines ++
      footer

  private def instanceLine: Seq[String] =
    List(
      s"  public static final $cl instance = new $cl();",
      ""
    )

}
