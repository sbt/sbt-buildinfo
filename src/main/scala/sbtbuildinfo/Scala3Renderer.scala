package sbtbuildinfo

abstract class Scala3Renderer extends ScalaRenderer {

  override protected def toJsonLines: Seq[String] =
    if (options contains BuildInfoOption.ToJson)
      List(
         """|  private def quote(x: scala.Any): String = "\"" + x + "\""
            |  private def toJsonValue[T <: Matchable](value: T): String = {
            |    value match {
            |      case elem: scala.collection.Seq[? <: Matchable] => elem.map(toJsonValue).mkString("[", ",", "]")
            |      case elem: scala.Option[? <: Matchable] => elem.map(toJsonValue).getOrElse("null")
            |      case elem: scala.collection.Map[?, ? <: Matchable] => elem.map {
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
}
