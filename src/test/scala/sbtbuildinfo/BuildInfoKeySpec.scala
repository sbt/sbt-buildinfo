package sbtbuildinfo

import sbt._, Keys._
import BuildInfoPlugin.autoImport._

/** This is a compile-only test of the BuildInfoKey syntax/macros. */
object BuildInfoKeySpec {
  buildInfoKeys  := Seq(name, version)                         // test `:=` works with setting keys
  buildInfoKeys  := Seq(products, fullClasspath)               // test `:=` works with task keys
  buildInfoKeys  := Seq(name, fullClasspath)                   // test `:=` works with setting and task keys
  buildInfoKeys  := Seq(                                       // test `:=` works with misc things
    name,
    fullClasspath,
    "year" -> 2012,
    BuildInfoKey.action("buildTime") { 1234L },
    BuildInfoKey.map(version) { case (_, v) => "projectVersion" -> v.toDouble },
    BuildInfoKey.map(fullClasspath) { case (ident, cp) => ident -> cp.files },
  )

  buildInfoKeys  += name                                       // test `+=` works with a setting key
//buildInfoKeys  += fullClasspath                              // test `+=` works with a task key
  buildInfoKeys  += (fullClasspath: BuildInfoKey)              // test `+=` works with a task key
  buildInfoKeys  += "year" -> 2012                             // test `+=` works with constants
  buildInfoKeys  += BuildInfoKey.action("buildTime") { 1234L } // test `+=` works with BuildInfoKey's
  buildInfoKeys  += BuildInfoKey.map(version) { case (_, v) => "projectVersion" -> v.toDouble }

  buildInfoKeys ++= Seq(name, version)                         // test `++=` works with setting keys
  buildInfoKeys ++= Seq[BuildInfoKey](fullClasspath)           // test `++=` works with 1 task key
  buildInfoKeys ++= Seq[BuildInfoKey](products, fullClasspath) // test `++=` works with n task keys
  buildInfoKeys ++= Seq[BuildInfoKey](name, fullClasspath)     // test `++=` works with setting and task keys
  buildInfoKeys ++= Seq[BuildInfoKey](                         // test `++=` works with misc things
    name,
    fullClasspath,
    "year" -> 2012,
    BuildInfoKey.action("buildTime") { 1234L },
    BuildInfoKey.map(version) { case (_, v) => "projectVersion" -> v.toDouble },
    BuildInfoKey.map(fullClasspath) { case (ident, cp) => ident -> cp.files },
  )


  buildInfoKeys  := BuildInfoKey.ofN(name, version)           // test `:=` works with setting keys
  buildInfoKeys  := BuildInfoKey.ofN(products, fullClasspath) // test `:=` works with task keys
  buildInfoKeys  := BuildInfoKey.ofN(name, fullClasspath)     // test `:=` works with setting and task keys
  buildInfoKeys  := BuildInfoKey.ofN(                         // test `:=` works with misc things
    name,
    fullClasspath,
    "year" -> 2012,
    BuildInfoKey.action("buildTime") { 1234L },
    BuildInfoKey.map(version) { case (_, v) => "projectVersion" -> v.toDouble },
    BuildInfoKey.map(fullClasspath) { case (ident, cp) => ident -> cp.files },
  )

  buildInfoKeys  += BuildInfoKey.of(name)                     // test `+=` works with a setting key
  buildInfoKeys  += BuildInfoKey.of(fullClasspath)            // test `+=` works with a task key

  buildInfoKeys ++= BuildInfoKey.ofN(name, version)           // test `++=` works with setting keys
  buildInfoKeys ++= BuildInfoKey.ofN(fullClasspath)           // test `++=` works with 1 task key
  buildInfoKeys ++= BuildInfoKey.ofN(products, fullClasspath) // test `++=` works with n task keys
  buildInfoKeys ++= BuildInfoKey.ofN(name, fullClasspath)     // test `++=` works with setting and task keys
  buildInfoKeys ++= BuildInfoKey.ofN(                         // test `++=` works with misc things
    name,
    fullClasspath,
    "year" -> 2012,
    BuildInfoKey.action("buildTime") { 1234L },
    BuildInfoKey.map(version) { case (_, v) => "projectVersion" -> v.toDouble },
    BuildInfoKey.map(fullClasspath) { case (ident, cp) => ident -> cp.files },
  )
}
