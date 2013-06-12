package ro.igstan.lixp

import scala.xml.{ Node, NodeSeq, Text }
import scala.xml.Utility.trim

object parser {
  def parseProgram(program: Node): Seq[Expr] = trim(program) match {
    case <program>{ ps @ _* }</program> => ps.map(parse(_))
  }

  def parse(xml: Node): Expr = {
    trim(xml) match {
      case <def><params>{ paramElems @ _* }</params>{ bodyElem }</def> =>
        val params = parseParams(paramElems)
        val body = parse(bodyElem)
        Def(params, body)
      case <call><fn>{ fnElem }</fn>{ argElems @ _* }</call> =>
        val fn = parseFn(fnElem)
        val args = parseArgs(argElems)
        App(fn, args)
      case <call><def>{ defElem @ _* }</def>{ argElems @ _* }</call> =>
        val fn = parse(<def>{ defElem }</def>)
        val args = parseArgs(argElems)
        App(fn, args)
      case <if>{ parts @ _* }</if> =>
        val cond   = parse(parts(0))
        val truthy = parse(parts(1))
        val falsy  = parse(parts(2))
        If(cond, truthy, falsy)
      case l @ <lit>{ Text(v) }</lit> =>
        l.attribute("type").map { t =>
          if (t(0).text == "int") Num(v.toInt) else {
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

  private def parseParams(params: NodeSeq): Seq[Symbol] =
    params.map {
      case param @ <param/> =>
        param.attribute("name").map { a => Symbol(a(0).text) } getOrElse {
          sys.error("required attribute `name' was missing")
        }
    }

  private def parseArgs(args: NodeSeq): Seq[Expr] =
    args.map { case <arg>{ arg }</arg> => parse(arg) }

  private def parseFn(fn: Node): Expr =
    fn match {
      case ref @ <ref/> =>
        ref.attribute("id").map {
          attr => Id(Symbol(attr(0).text))
        } getOrElse {
          sys.error("required attribute `id' was missing")
        }
      case a => parse(a)
    }
}
