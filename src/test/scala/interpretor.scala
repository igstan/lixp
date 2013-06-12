package ro.igstan.test.lixp

import ro.igstan.lixp._

class InterpretorSpec extends FunSuite with MustMatchers {
  test("interprets from XML nodes") {
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
    </program>

    val result = DefValue(Set('a, 'b), App(Id('+), List(Id('a), Id('b))), lixp.standardEnv)

    lixp.evaluate(parser.parseProgram(code)(0)) must be(Right(result))
  }

  test("interprets immediate application of a lambda") {
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

    val result = NumValue(9)

    lixp.evaluate(parser.parseProgram(code)(0)) must be(Right(result))
  }

  test("interprets Y combinator") {
    val Y = <def>
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

    val factorial = <call>
      { Y }
      <arg>
        <def>
          <params><param name="factorial"/></params>
          <def>
            <params><param name="n"/></params>
            <if>
              <ref id="n"/>
              <call>
                <fn><ref id="*"/></fn><arg><ref id="n"/></arg>
                <arg>
                  <call>
                    <fn><ref id="factorial"/></fn>
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

    val `5!` = <call>
      <fn>{ factorial }</fn>
      <arg><lit type="int">5</lit></arg>
    </call>

    lixp.evaluate(parser.parse(`5!`)) must be(Right(NumValue(120)))
  }
}
