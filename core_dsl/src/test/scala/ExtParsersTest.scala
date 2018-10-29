import hu.tigra.start.dependencies.ExtParsers
import org.scalatest.FunSuite

/**
  * https://stackoverflow.com/questions/44359700/not-ordered-parsers-in-scala-parser-combinators
  */
class ExtParsersTest extends FunSuite {

  object MyParser extends ExtParsers {

    def unord: Parser[String] =
      (ident $ stringLiteral $ wholeNumber $ floatingPointNumber) ^^ {
        case id $ sl $ wn $ fpn =>
          s"ident=$id string=$sl int=$wn float=$fpn"
      }
  }

  test("order should not matter") {
    val expected = "ident=value string=\"test\" int=10 float=10.99"

    assert(MyParser.parseAll(MyParser.unord, "value \"test\" 10 10.99").get == expected)

    assert(MyParser.parseAll(MyParser.unord, "\"test\" value  10 10.99").get == expected)

    assert(MyParser.parseAll(MyParser.unord, "10 value \"test\" 10.99").get == expected)
  }
}