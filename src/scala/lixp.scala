package ro.igstan.lixp

import scala.collection.immutable.ListSet

sealed trait Expr
case class Num(n: Int) extends Expr
case class Id(id: Symbol) extends Expr
case class If(test: Expr, truthy: Expr, falsy: Expr) extends Expr
case class Let(bindings: Seq[(Symbol, Expr)], body: Expr) extends Expr
case class Def(params: Seq[Symbol], body: Expr) extends Expr
case class App(fn: Expr, args: Seq[Expr]) extends Expr

sealed trait Value
case class NumValue(n: Int) extends Value
case class DefValue(params: Set[Symbol], body: Expr, env: Map[Symbol, Value]) extends Value
case class NativeDefValue(fn: Seq[Value] => Either[String, Value]) extends Value

object lixp {
  private def key[K,V](kv: (K,V)): K = kv._1
  private def byValue[K,V](fn: V => Boolean)(kv: (K,V)): Boolean = fn(kv._2)

  val standardEnv = Map(
    '+ -> stdlib.add,
    '- -> stdlib.sub,
    '* -> stdlib.mul,
    '/ -> stdlib.div
  )

  def evaluate(expr: Expr, env: Map[Symbol, Value] = standardEnv): Either[String, Value] = expr match {
    case Num(n) => Right(NumValue(n))
    case Id(id) => env.get(id) match {
        case None    => Left("unbound identifier: %s".format(id.name))
        case Some(v) => Right(v)
      }
    case If(test, truthy, falsy) => evaluate(test, env) match {
        case (Right(NumValue(0))) => evaluate(falsy, env)
        case (Right(NumValue(_))) => evaluate(truthy, env)
        case (Right(_))           => Left("test condition in if expression is not a number")
        case a                    => a
      }
    case Let(bindings, body) => {
      val dupes = bindings.groupBy(key).mapValues(_.size).filter(byValue(_ > 1))

      if (dupes.size > 0) {
        Left("duplicate binding occurrences: %s".format(dupes.keys.map(_.name).mkString("; ")))
      } else {
        val boundEnv = bindings.foldLeft[Either[String, Map[Symbol, Value]]](Right(env)) { (prevEnv, binding) =>
          prevEnv match {
            case Right(prevEnv) =>
              evaluate(binding._2, env) match {
                case Right(value)  => Right(prevEnv + (binding._1 -> value))
                case Left(message) => Left(message)
              }
            case left => left
          }
        }

        boundEnv match {
          case Right(boundEnv) => evaluate(body, boundEnv)
          case Left(message)   => Left(message)
        }
      }
    }
    case Def(params, body) => {
      val dupes = params.groupBy(identity).mapValues(_.size).filter(byValue(_ > 1))

      if (dupes.size > 0) {
        // extract duplicate param names preserving declaration order
        val symbolSet = ListSet(params.reverse : _*)
        val dupeParams = dupes.keys.toSet
        val dupeNames = symbolSet.filter(dupeParams contains _)
        Left("duplicate parameter occurrences: %s".format(dupeNames.map(_.name).mkString("; ")))
      } else {
        Right(DefValue(params.toSet, body, env))
      }
    }
    case App(fn, args) => {
      evaluate(fn, env) match {
        case Left(message) => Left(message)
        case Right(DefValue(params, body, closedEnv)) => {
          val boundEnv = (params, args).zipped.toList.foldLeft[Either[String, Map[Symbol, Value]]](Right(closedEnv)) {
            (prevEnv, binding) =>
              prevEnv match {
                case Right(prevEnv) =>
                  evaluate(binding._2, env) match {
                    case Right(value)  => Right(prevEnv + (binding._1 -> value))
                    case Left(message) => Left(message)
                  }
                case left => left
              }
          }

          boundEnv match {
            case Right(boundEnv) => evaluate(body, boundEnv)
            case Left(message)   => Left(message)
          }
        }
        case Right(NativeDefValue(nfn)) =>
          val evaledArgs = args.foldLeft[Either[String, Seq[Value]]](Left(s"no arguments given for $fn")) {
            case (Right(values), arg) =>
              evaluate(arg, env).right.map(_ +: values)
            case (Left(m), arg) if m.startsWith("no arguments given for") =>
              evaluate(arg, env).right.map(Seq(_))
            case (Left(m), _) => Left(m)
          }

          evaledArgs.right.flatMap(args => nfn(args.reverse))
        case Right(r) => Left(s"non-function in application position: $r")
      }
    }
  }
}
