package hu.tigra.start.dependencies

import scala.util.parsing.combinator.JavaTokenParsers

/**
  * https://stackoverflow.com/questions/44359700/not-ordered-parsers-in-scala-parser-combinators
  */
trait ExtParsers extends JavaTokenParsers {
  def unordered[T, U](tp: Parser[T], tu: Parser[U]): Parser[$[T, U]] =
    tp ~ tu ^^ { case x ~ y => $(x, y) } |
      tu ~ tp ^^ { case x ~ y => $(y, x) }

  case class $[+A, +B](_1: A, _2: B)

  implicit class ExtParser[+T](val parser: Parser[T]) {
    def $[U](tu: Parser[U]): Parser[$[T, U]] = unordered(parser, tu)
  }

}
