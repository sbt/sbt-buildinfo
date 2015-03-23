package sbtbuildinfo

// Copied from scala/pickling
// https://github.com/scala/pickling/blob/c0fc5df7784188cf470debb3f9d41adaf37df5a6/core/src/main/scala/pickling/internal/AppliedType.scala

object TypeExpression {
  // the delimiters in an applied type
  private val delims = List(',', '[', ']')

  /* Parse an applied type.
   *
   * @param  s the string that is parsed
   * @return   a pair with the parsed applied type and the remaining string.
   */
  def parse(s: String): (TypeExpression, String) = {
    // shape of `s`: fqn[at_1, ..., at_n]
    val (typeName, rem) = s.trim.span(!delims.contains(_))

    if (rem.isEmpty || rem.startsWith(",") || rem.startsWith("]")) {
      (TypeExpression(typeName, List()), rem)
    } else { // parse type arguments
      var typeArgs = List[TypeExpression]()
      var remaining = rem

      while (remaining.startsWith("[") || remaining.startsWith(",")) {
        remaining = remaining.substring(1)
        val (next, rem) = parse(remaining)
        typeArgs = typeArgs :+ next
        remaining = rem
      }

      (TypeExpression(typeName, typeArgs), if (remaining.startsWith("]")) remaining.substring(1) else remaining)
    }
  }

}

/**
 * Simple representation of an applied type. Used for reading pickled types.
 *
 * Example,  ``List[String]`` would be represented as:
 *
 * {{{
 *   TypeExpression("scala.collection.immutable.List",
 *      Seq(TypeExpression("java.lang.String", Nil)
 *   )
 * }}}
 *
 * As you can see, simple types like "String" are represented as applied types with no arguments.
 */
case class TypeExpression(typeName: String, typeArgs: List[TypeExpression]) {
  override def toString =
    typeName + (if (typeArgs.isEmpty) "" else typeArgs.mkString("[", ",", "]"))
}
