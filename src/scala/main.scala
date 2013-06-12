package ro.igstan.lixp

import scala.xml.XML

object Main {
  def main(args: Array[String]): Unit = {
    val source = XML.loadFile(args(0))
    lixp.evaluateSeq(parser.parseProgram(source))
  }
}
