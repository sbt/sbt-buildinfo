package sbtbuildinfo

import PluginCompat.TypeExpression

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

  protected def getType(m: PluginCompat.Manifest[?]): Option[String] = {
    def tpeToReturnType(m: PluginCompat.Manifest[?]): Option[String] =
      m match {
        case TypeExpression("Any", Nil)    => None
        case TypeExpression("Int" | "scala.Int", Nil) => Some("scala.Int")
        case TypeExpression("Long" | "scala.Long", Nil)  => Some("scala.Long")
        case TypeExpression("Double" | "scala.Double", Nil) => Some("scala.Double")
        case TypeExpression("Boolean" | "scala.Boolean" | "java.lang.Boolean", Nil) => Some("scala.Boolean")
        case TypeExpression("scala.Symbol", Nil) => Some("scala.Symbol")
        case TypeExpression("java.lang.String", Nil) => Some("String")
        case TypeExpression("java.net.URL", Nil) => Some("java.net.URL")
        case TypeExpression("sbt.URL", Nil) => Some("java.net.URL")
        case TypeExpression("java.net.URI", Nil) => Some("java.net.URI")
        case TypeExpression("java.io.File", Nil) => Some("java.io.File")
        case TypeExpression("sbt.File", Nil) => Some("java.io.File")
        case TypeExpression("scala.xml.NodeSeq", Nil) => Some("scala.xml.NodeSeq")

        case TypeExpression("sbt.ModuleID", Nil) => Some("String")
        case TypeExpression("sbt.Resolver", Nil) => Some("String")
        case TypeExpression("xsbti.HashedVirtualFileRef", Nil) => Some("String")
        case TypeExpression("xsbti.VirtualFileRef", Nil) => Some("String")
        case TypeExpression("xsbti.VirtualFile", Nil) => Some("String")

        case TypeExpression("sbt.librarymanagement.ModuleID", Nil) => Some("String")
        case TypeExpression("sbt.librarymanagement.Resolver", Nil) => Some("String")

        case TypeExpression("sbt.internal.util.Attributed", Seq(TypeExpression("java.io.File", Nil))) => Some("java.io.File")
        case TypeExpression("sbt.internal.util.Attributed", Seq(TypeExpression("xsbti.HashedVirtualFileRef", Nil))) => Some("String")

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

        case TypeExpression("java.time.LocalDate", Nil) => Some("java.time.LocalDate")
        case TypeExpression("java.time.Instant", Nil)   => Some("java.time.Instant")

        case _ =>
          // println(s"tpe: $m")
          None
      }
    tpeToReturnType(m)
  }

  protected def quote(v: Any): String = v match {
    case x @ ( _: Int | _: Double | _: Boolean) => x.toString
    case x: Symbol          => s"""scala.Symbol("${x.name}")"""
    case x: Long            => x.toString + "L"
    case node: scala.xml.NodeSeq if node.toString().trim.nonEmpty => node.toString()
    case _: scala.xml.NodeSeq => "scala.xml.NodeSeq.Empty"
    case (k, _v)            => s"(${quote(k)} -> ${quote(_v)})"
    case mp: Map[?, ?]      => mp.toList.map(quote(_)).mkString("Map(", ", ", ")")
    case seq: collection.Seq[?] => seq.map(quote).mkString("scala.collection.immutable.Seq(", ", ", ")")
    case op: Option[?]      => op map { x => "scala.Some(" + quote(x) + ")" } getOrElse {"scala.None"}
    case url: java.net.URL  => s"new java.net.URI(${quote(url.toString)}).toURL"
    case uri: java.net.URI  => s"new java.net.URI(${quote(uri.toString)})"
    case file: java.io.File => s"new java.io.File(${quote(file.toString)})"
    case attr: sbt.Attributed[?] => quote(attr.data)
    case date: java.time.LocalDate  => s"java.time.LocalDate.parse(${quote(date.toString)})"
    case instant: java.time.Instant => s"java.time.Instant.parse(${quote(instant.toString)})"
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
