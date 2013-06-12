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
          <ref id="+"/>
          <ref id="a"/>
          <ref id="b"/>
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
            <ref id="+"/>
            <ref id="a"/>
            <ref id="b"/>
          </call>
        </def>
        <lit type="int">4</lit>
        <lit type="int">5</lit>
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

    val factorial = <call>
      { Y }
      <def>
        <params><param name="factorial"/></params>
        <def>
          <params><param name="n"/></params>
          <if>
            <ref id="n"/>
            <call>
              <ref id="*"/>
              <ref id="n"/>
              <call>
                <ref id="factorial"/>
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

    val `5!` = <call>
      { factorial }
      <lit type="int">5</lit>
    </call>

    lixp.evaluate(parser.parse(`5!`)) must be(Right(NumValue(120)))
  }
}
