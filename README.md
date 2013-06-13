# Lixp

Lixp is a tiny programming language bearing basic Scheme semantics behind a hideous XML syntax.

## How To Run

    $ sbt assembly
    $ java -jar target/scala-2.10/lixp-assembly-0.1.0.jar examples/y-factorial.lixp.xml
    120

## How It Looks

```xml
<program>
  <!-- The famous Y combinator Lixp style! -->
  <def name="Y">
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

  <!-- The factorial function implemented using recursion via Y combinator -->
  <def name="factorial">
    <call>
      <ref id="Y"/>
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
  </def>

  <call>
    <ref id="print"/>
    <call>
      <ref id="factorial"/>
      <lit type="int">5</lit>
    </call>
  </call>
</program>
```

## FAQs

Q: Is this a joke?<br>
A: Yes!

Q: Does it really work?<br>
A: Yes. Well, for the most part. I haven't yet implemented recusion. That's why
   you'll see the Y combinator in a lot of places.
