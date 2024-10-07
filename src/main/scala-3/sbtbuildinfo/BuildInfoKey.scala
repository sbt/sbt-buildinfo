package sbtbuildinfo

import sbt.*
import scala.reflect.ClassTag
import scala.quoted.*

object BuildInfoKey:
  import Entry.*

  inline def apply[A1](inline key: SettingKey[A1]): Entry[A1] =
    ${ entrySetting('key) }
  private def entrySetting[A1: Type](expr: Expr[SettingKey[A1]])(using qctx: Quotes): Expr[Entry[A1]] =
    import qctx.reflect.*
    val tpe0 = TypeRepr.of[A1]
    '{ Entry.Setting($expr, ${ Expr(tpe0.show) }) }

  inline def apply[A1](inline key: TaskKey[A1]): Entry[A1] =
    ${ entryTask('key) }
  private def entryTask[A1: Type](expr: Expr[TaskKey[A1]])(using qctx: Quotes): Expr[Entry[A1]] =
    import qctx.reflect.*
    val tpe0 = TypeRepr.of[A1]
    '{ Entry.Task($expr, ${ Expr(tpe0.show) }) }

  inline def apply[A1](inline tuple: (String, A1)): Entry[A1] =
    ${ entryConstant('tuple) }
  private def entryConstant[A1: Type](expr: Expr[(String, A1)])(using qctx: Quotes): Expr[Entry[A1]] =
    import qctx.reflect.*
    val tpe0 = TypeRepr.of[A1]
    '{ Entry.Constant($expr, ${ Expr(tpe0.show) }) }

  inline def map[A1, A2](inline from: Entry[A1])(inline fun: ((String, A1)) => (String, A2)): Entry[A2] =
    ${ entryMapped('from, 'fun) }

  private def entryMapped[A1: Type, A2: Type](from: Expr[Entry[A1]], fun: Expr[((String, A1)) => (String, A2)])(using qctx: Quotes): Expr[Entry[A2]] =
    import qctx.reflect.*
    val tpe0 = TypeRepr.of[A2]
    '{ Entry.Mapped($from, $fun, ${ Expr(tpe0.show) }) }

  inline def action[A1](inline name: String)(inline fun: A1): Entry[A1] =
    ${ entryAction('name, 'fun) }
  private def entryAction[A1: Type](name: Expr[String], fun: Expr[A1])(using qctx: Quotes): Expr[Entry[A1]] =
    import qctx.reflect.*
    val tpe0 = TypeRepr.of[A1]
    '{ Entry.Action($name, () => $fun, ${ Expr(tpe0.show) }) }

  inline def outOfGraphUnsafe[A1](inline key: TaskKey[A1]): Entry[A1] =
    ${ entryTask1('key) }
  private def entryTask1[A1: Type](expr: Expr[TaskKey[A1]])(using qctx: Quotes): Expr[Entry[A1]] =
    import qctx.reflect.*
    val tpe0 = TypeRepr.of[A1]
    '{ Entry.Task($expr, ${ Expr(tpe0.show) }) }
end BuildInfoKey
