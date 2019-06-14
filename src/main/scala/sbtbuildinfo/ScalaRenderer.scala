package sbtbuildinfo

abstract class ScalaRenderer extends BuildInfoRenderer {
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
    case x @ ( _: Int | _: Double | _: Boolean | _: Symbol) => x.toString
    case x: Long            => x.toString + "L"
    case node: scala.xml.NodeSeq if node.toString().trim.nonEmpty => node.toString()
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
