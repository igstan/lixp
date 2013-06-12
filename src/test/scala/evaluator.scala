package ro.igstan.test.lixp

import ro.igstan.lixp._

class EvaluatorSpec extends FunSuite with MustMatchers {
  test("evaluates numbers") {
    lixp.evaluate(Num(5)) must be(Right(NumValue(5)))
  }

  test("finds identifier in environment") {
    lixp.evaluate(Id('foo), Binding('foo, NumValue(4), Empty())) must be(Right(NumValue(4)))
  }

  test("complains when it can't find identifier in environment") {
    lixp.evaluate(Id('foo), Empty()) must be(Left("unbound identifier: foo"))
  }

  test("single-binding let expression") {
    lixp.evaluate(Let(List('foo -> Num(1)), Id('foo)), Empty()) must be(Right(NumValue(1)))
  }

  test("let expression errors with duplicate bindings 1") {
    val letExpr = Let(List('foo -> Num(1), 'foo -> Num(1)), Id('foo))
    lixp.evaluate(letExpr, Empty()) must be(Left("duplicate binding occurrences: foo"))
  }

  test("let expression errors with duplicate bindings 2") {
    val letExpr = Let(
      List(
        'foo -> Num(1),
        'foo -> Num(1),
        'bar -> Num(2),
        'bar -> Num(2)
      ),
      Id('foo)
    )
    lixp.evaluate(letExpr, Empty()) must be(Left("duplicate binding occurrences: foo; bar"))
  }

  test("anonymous function definition") {
    val expr = Def(params = List('a, 'b), body = Id('a))
    val eval = Right(DefValue(Set('a, 'b), Id('a), lixp.standardEnv))
    lixp.evaluate(expr) must be(eval)
  }

  test("anonymous function definition with closure") {
    val expr = Let(List('a -> Num(1)), Def(params = List('a, 'b), body = Id('a)))
    var eval = DefValue(Set('a, 'b), Id('a), Binding('a, NumValue(1), lixp.standardEnv))
    lixp.evaluate(expr) must be(Right(eval))
  }

  test("anonymous function definition with a single duplicate parameter") {
    val expr = Def(params = List('a, 'a), body = Id('a))
    val eval = Left("duplicate parameter occurrences: a")
    lixp.evaluate(expr) must be(eval)
  }

  test("anonymous function definition with multiple duplicate parameters") {
    var expr = Def(params = List('a, 'a, 'b, 'b), body = Id('a))
    val eval = Left("duplicate parameter occurrences: a; b")
    lixp.evaluate(expr) must be(eval)
  }

  test("simple function application") {
    val fn   = Def(List('a, 'b), body = App(Id('+), List(Id('a), Id('b))))
    val app  = App(fn, args = List(Num(1), Num(3)))
    val eval = Right(NumValue(4))

    lixp.evaluate(app) must be(eval)
  }

  test("factorial function using the Y combinator") {
    val Y = Def(List('h),
      App(
        Def(List('f),
          App(Id('f), List(Id('f)))),
        List(Def(List('f),
          App(Id('h),
            List(Def(List('n),
              App(App(Id('f), List(Id('f))), List(Id('n))))))))))

    val fact = App(Y, List(
      Def(List('g),
        Def(List('n),
          If(
            Id('n),
            App(Id('*), List(Id('n), App(Id('g), List(App(Id('-), List(Id('n), Num(1))))))),
            Num(1))))))

    val expr = Let(List('fact -> fact), App(Id('fact), List(Num(5))))
    val eval = Right(NumValue(120))

    lixp.evaluate(expr) must be(eval)
  }
}
