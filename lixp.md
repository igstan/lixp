## The Y Combinator

```xml
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
```

## Factorial

```racket
(define (factorial n)
  (if (< n 2) 1
      (* n (factorial (- n 1)))))

(factorial 5)
```

```xml
<program>
  <def name="factorial">
    <params>
      <param name="n"/>
    </params>
    <if>
      <call>
        <fn><ref id="<"/></fn>
        <arg><ref id="n"/></arg>
        <arg><lit type="int">2</lit></arg>
      </call>
      <lit type="int">1</lit>
      <call>
        <fn><ref id="*"/></fn>
        <arg><ref id="n"/></arg>
        <arg debug="yes">
          <call>
            <fn><ref id="factorial"/></fn>
            <arg><ref id="n"/></arg>
            <arg><lit type="int">1</lit></arg>
          </call>
        </arg>
      </call>
    </if>
  </def>
  <call>
    <fn><ref id="factorial"/></fn>
    <arg><lit type="int">5</lit></arg>
  </call>
</program>
```
