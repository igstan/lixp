package ro.igstan.test.lixp

import ro.igstan.lixp._

class ParserSpec extends FunSuite with MustMatchers {
  test("parses a program") {
    val code = <program>
      <def name="add">
        <params>
          <param name="a"/>
          <param name="b"/>
        </params>
        <call>
          <ref id="+"/>
          <ref id="a"/>
          <ref id="b"/>
        </call>
      </def>
      <call>
        <ref id="add"/>
        <lit type="int">4</lit>
        <lit type="int">5</lit>
      </call>
    </program>

    val ast = Seq(
      Def(List('a, 'b), App(Id('+), List(Id('a), Id('b)))),
      App(Id('add), List(Num(4), Num(5)))
    )

    parser.parseProgram(code) must be(ast)
  }

  test("parses XML nodes representing an immediate application of a lambda") {
    val code = <call>
      <def>
        <params>
          <param name="a"/>
          <param name="b"/>
        </params>
        <call>
          <ref id="+"/>
          <ref id="a"/>
          <ref id="b"/>
        </call>
      </def>
      <lit type="int">4</lit>
      <lit type="int">5</lit>
    </call>

    val ast = App(
      Def(List('a, 'b), App(Id('+), List(Id('a), Id('b)))),
      List(Num(4), Num(5))
    )

    parser.parse(code) must be(ast)
  }

  test("parses factorial defined using the Y combinator") {
    val xml = <call>
      <def>
        <params><param name="h"/></params>
        <call>
          <def>
            <params><param name="f"/></params>
            <call><ref id="f"/><ref id="f"/></call>
          </def>
          <def>
            <params><param name="f"/></params>
            <call>
              <ref id="h"/>
              <def>
                <params><param name="n"/></params>
                <call>
                  <call><ref id="f"/><ref id="f"/></call>
                  <ref id="n"/>
                </call>
              </def>
            </call>
          </def>
        </call>
      </def>
      <def>
        <params><param name="g"/></params>
        <def>
          <params><param name="n"/></params>
          <if>
            <ref id="n"/>
            <call>
              <ref id="*"/>
              <ref id="n"/>
              <call>
                <ref id="g"/>
                <call>
                  <ref id="-"/>
                  <ref id="n"/>
                  <lit type="int">1</lit>
                </call>
              </call>
            </call>
            <lit type="int">1</lit>
          </if>
        </def>
      </def>
    </call>

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

    parser.parse(xml) must be(fact)
  }
}
