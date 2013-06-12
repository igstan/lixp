package ro.igstan.test.lixp

import ro.igstan.lixp._

class ParserSpec extends FunSuite with MustMatchers {
  test("parses XML nodes") {
    val code = <program>
      <def name="add">
        <params>
          <param name="a"/>
          <param name="b"/>
        </params>
        <call>
          <fn><ref id="+"/></fn>
          <arg><ref id="a"/></arg>
          <arg><ref id="b"/></arg>
        </call>
      </def>
      <call>
        <fn><ref id="add"/></fn>
        <arg><lit type="int">4</lit></arg>
        <arg><lit type="int">5</lit></arg>
      </call>
    </program>

    val ast = Seq(
      Def(List('a, 'b), App(Id('+), List(Id('a), Id('b)))),
      App(Id('add), List(Num(4), Num(5)))
    )

    parser.parseProgram(code) must be(ast)
  }

  test("parses XML nodes representing an immediate application of a lambda") {
    val code = <program>
      <call>
        <def>
          <params>
            <param name="a"/>
            <param name="b"/>
          </params>
          <call>
            <fn><ref id="+"/></fn>
            <arg><ref id="a"/></arg>
            <arg><ref id="b"/></arg>
          </call>
        </def>
        <arg><lit type="int">4</lit></arg>
        <arg><lit type="int">5</lit></arg>
      </call>
    </program>

    val ast = Seq(
      App(Def(List('a, 'b), App(Id('+), List(Id('a), Id('b)))), List(Num(4), Num(5)))
    )

    parser.parseProgram(code) must be(ast)
  }

  test("parses factorial defined using the Y combinator") {
    val xml = <call>
      <def>
        <params><param name="h"/></params>
        <call>
          <def>
            <params><param name="f"/></params>
            <call><fn><ref id="f"/></fn><arg><ref id="f"/></arg></call>
          </def>
          <arg>
            <def>
              <params><param name="f"/></params>
              <call>
                <fn><ref id="h"/></fn>
                <arg>
                  <def>
                    <params><param name="n"/></params>
                    <call>
                      <fn><call><fn><ref id="f"/></fn><arg><ref id="f"/></arg></call></fn>
                      <arg><ref id="n"/></arg>
                    </call>
                  </def>
                </arg>
              </call>
            </def>
          </arg>
        </call>
      </def>
      <arg>
        <def>
          <params><param name="g"/></params>
          <def>
            <params><param name="n"/></params>
            <if>
              <ref id="n"/>
              <call>
                <fn><ref id="*"/></fn><arg><ref id="n"/></arg>
                <arg>
                  <call>
                    <fn><ref id="g"/></fn>
                    <arg>
                      <call>
                        <fn><ref id="-"/></fn>
                        <arg><ref id="n"/></arg>
                        <arg><lit type="int">1</lit></arg>
                      </call>
                    </arg>
                  </call>
                </arg>
              </call>
              <lit type="int">1</lit>
            </if>
          </def>
        </def>
      </arg>
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
