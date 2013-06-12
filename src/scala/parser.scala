package ro.igstan.lixp

import scala.xml.{ Node, NodeSeq, Text }
import scala.xml.Utility.trim

object parser {
  def parseProgram(program: Node): Seq[Expr] = trim(program) match {
    case <program>{ ps @ _* }</program> => ps.map(parse(_))
  }

  def parse(xml: Node): Expr = {
    trim(xml) match {
      case d @ <def><params>{ ps @ _* }</params>{ body }</def> =>
        val result = Def(params(ps), parse(body))
        d.attribute("name").map {
          attr => Let(Seq(Symbol(attr(0).text) -> result), Id(Symbol(attr(0).text)))
        } getOrElse result

      case d @ <def>{ body }</def> =>
        val result = parse(body)
        d.attribute("name").map {
          attr => Let(Seq(Symbol(attr(0).text) -> result), Id(Symbol(attr(0).text)))
        } getOrElse {
          sys.error("binding must be named")
        }

      case <call>{ fnElem }{ argElems @ _* }</call> =>
        val fn = parse(fnElem)
        val args = argElems.map(parse(_))
        App(fn, args)

      case <if>{ parts @ _* }</if> =>
        If(parse(parts(0)), parse(parts(1)), parse(parts(2)))

      case lit @ <lit>{ Text(v) }</lit> =>
        lit.attribute("type").map { tip =>
          if (tip(0).text == "int") Num(v.toInt) else {
            sys.error("unsupported value for attribute `type'")
          }
        } getOrElse {
          sys.error("required attribute `type' was missing")
        }

      case p @ <ref/> =>
        p.attribute("id").map { a => Id(Symbol(a(0).text)) } getOrElse {
          sys.error("required attribute `id' was missing")
        }
    }
  }

  private def params(params: NodeSeq): Seq[Symbol] =
    params.map {
      case param @ <param/> =>
        param.attribute("name").map { a => Symbol(a(0).text) } getOrElse {
          sys.error("required attribute `name' was missing")
        }
    }
}
