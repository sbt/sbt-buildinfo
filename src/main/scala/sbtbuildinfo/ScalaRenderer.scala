package sbtbuildinfo

abstract class ScalaRenderer extends BuildInfoRenderer {

  protected def pkg: String

  def options: Seq[BuildInfoOption]

  protected def toMapLines(results: Seq[BuildInfoResult]): Seq[String] =
    if (options.contains(BuildInfoOption.ToMap) || options.contains(BuildInfoOption.ToJson))
      results
        .map(result => "    \"%s\" -> %s".format(result.identifier, result.identifier))
        .mkString("  val toMap: Map[String, scala.Any] = Map[String, scala.Any](\n", ",\n", ")")
        .split("\n")
        .toList ::: List("")
    else Nil

  protected def toJsonLines: Seq[String] =
    if (options contains BuildInfoOption.ToJson)
      List(
         """|  private def quote(x: scala.Any): String = "\"" + x + "\""
            |  private def toJsonValue(value: scala.Any): String = {
            |    value match {
            |      case elem: scala.collection.Seq[_] => elem.map(toJsonValue).mkString("[", ",", "]")
            |      case elem: scala.Option[_] => elem.map(toJsonValue).getOrElse("null")
            |      case elem: scala.collection.Map[_, scala.Any] => elem.map {
            |        case (k, v) => toJsonValue(k.toString) + ":" + toJsonValue(v)
            |      }.mkString("{", ", ", "}")
            |      case d: scala.Double => d.toString
            |      case f: scala.Float => f.toString
            |      case l: scala.Long => l.toString
            |      case i: scala.Int => i.toString
            |      case s: scala.Short => s.toString
            |      case bool: scala.Boolean => bool.toString
            |      case str: String => quote(str)
            |      case other => quote(other.toString)
            |    }
            |  }
            |
            |  val toJson: String = toJsonValue(toMap)""".stripMargin)
    else Nil

  protected def getType(typeExpr: TypeExpression): Option[String] = {
    def tpeToReturnType(tpe: TypeExpression): Option[String] =
      tpe match {
        case TypeExpression("Any", Nil)    => None
        case TypeExpression("Int", Nil)    => Some("scala.Int")
        case TypeExpression("Long", Nil)   => Some("scala.Long")
        case TypeExpression("Double", Nil) => Some("scala.Double")
        case TypeExpression("Boolean", Nil) => Some("scala.Boolean")
        case TypeExpression("scala.Symbol", Nil) => Some("scala.Symbol")
        case TypeExpression("java.lang.String", Nil) => Some("String")
        case TypeExpression("java.net.URL", Nil) => Some("java.net.URL")
        case TypeExpression("sbt.URL", Nil) => Some("java.net.URL")
        case TypeExpression("java.io.File", Nil) => Some("java.io.File")
        case TypeExpression("sbt.File", Nil) => Some("java.io.File")
        case TypeExpression("scala.xml.NodeSeq", Nil) => Some("scala.xml.NodeSeq")

        case TypeExpression("sbt.ModuleID", Nil) => Some("String")
        case TypeExpression("sbt.Resolver", Nil) => Some("String")

        case TypeExpression("sbt.librarymanagement.ModuleID", Nil) => Some("String")
        case TypeExpression("sbt.librarymanagement.Resolver", Nil) => Some("String")

        case TypeExpression("sbt.internal.util.Attributed", Seq(TypeExpression("java.io.File", Nil))) => Some("java.io.File")

        case TypeExpression("scala.Option", Seq(arg)) =>
          tpeToReturnType(arg) map { x => s"scala.Option[$x]" }
        case TypeExpression("scala.collection.Seq" | "scala.collection.immutable.Seq", Seq(arg)) =>
          tpeToReturnType(arg) map { x => s"scala.collection.immutable.Seq[$x]" }
        case TypeExpression("scala.collection.immutable.Map", Seq(arg0, arg1)) =>
          for {
            x0 <- tpeToReturnType(arg0)
            x1 <- tpeToReturnType(arg1)
          } yield s"Map[$x0, $x1]"
        case TypeExpression("scala.Tuple2", Seq(arg0, arg1)) =>
          for {
            x0 <- tpeToReturnType(arg0)
            x1 <- tpeToReturnType(arg1)
          } yield s"($x0, $x1)"
        case _ => None
      }
    tpeToReturnType(typeExpr)
  }

  protected def quote(v: Any): String = v match {
    case x @ ( _: Int | _: Double | _: Boolean) => x.toString
    case x: Symbol          => s"""scala.Symbol("${x.name}")"""
    case x: Long            => x.toString + "L"
    case node: scala.xml.NodeSeq if node.toString().trim.nonEmpty => node.toString()
    case node: scala.xml.NodeSeq => "scala.xml.NodeSeq.Empty"
    case (k, _v)            => "(%s -> %s)" format(quote(k), quote(_v))
    case mp: Map[_, _]      => mp.toList.map(quote(_)).mkString("Map(", ", ", ")")
    case seq: collection.Seq[_] => seq.map(quote).mkString("scala.collection.immutable.Seq(", ", ", ")")
    case op: Option[_]      => op map { x => "scala.Some(" + quote(x) + ")" } getOrElse {"scala.None"}
    case url: java.net.URL  => "new java.net.URL(%s)" format quote(url.toString)
    case file: java.io.File => "new java.io.File(%s)" format quote(file.toString)
    case attr: sbt.Attributed[_] => quote(attr.data)
    case s                  => "\"%s\"" format encodeStringLiteral(s.toString)
  }

  protected def encodeStringLiteral(str: String): String =
    str.replace("\\","\\\\").replace("\n","\\n").replace("\b","\\b").replace("\r","\\r").
      replace("\t","\\t").replace("\'","\\'").replace("\f","\\f").replace("\"","\\\"")

  protected def withPkgPriv(str: String): String =
    if(pkg.nonEmpty && isPkgPriv)
      s"private[${pkg.split('.').last}] $str"
    else str

}
