package sbtbuildinfo

import scala.reflect.macros.blackbox

final class BuildInfoKeyMacros(val c: blackbox.Context) {
  import c.universe._

  val BuildInfoKey = q"_root_.sbtbuildinfo.BuildInfoKey"

  def taskImpl(key: Tree): Tree = {
    val A = key.tpe.typeArgs.head
    q"$BuildInfoKey.sbtbuildinfoTaskValueEntry[$A]($key.taskValue)($key.key.manifest.typeArguments.head.asInstanceOf[_root_.scala.reflect.Manifest[$A]])"
  }
}
