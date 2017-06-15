package top.sandwwraith.mt.lab4.examples

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import top.sandwwraith.mt.lab4.examples.expr.ExprLexer
import top.sandwwraith.mt.lab4.examples.expr.ExprParser
import top.sandwwraith.mt.lab4.examples.hello.HelloLexer
import top.sandwwraith.mt.lab4.examples.hello.HelloParser

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

class ExamplesTester : WordSpec() {

    fun parse(s: String) = HelloParser(HelloLexer(s.reader())).parse()
    fun eval(s: String) = ExprParser(ExprLexer(s.reader())).parse()

    init {
        "Hello grammar" should {
            "format 'hello Leo'" {
                parse("hello Leo") shouldBe "Hello, Leo!"
            }
        }
        "Expressions grammar" should {
            "eval '2 + 2'" {
                eval("2 + 2") shouldBe 4
            }
            "eval '2+3 * 4'" {
                eval("2+3 * 4") shouldBe 14
            }
            "eval '(2 + 3)*4'" {
                eval("(2 + 3)*4") shouldBe 20
            }
            "eval '(2e3 * 3e2 + 10) * 2'" {
                eval("(2e3 * 3e2 + 10) * 2") shouldBe (((2 * 2 * 2) * (3 * 3) + 10) * 2)
            }
            "eval '2 ** 3 ** 2 * 3'" {
                eval("2 ** 3 ** 2 * 3 + 3") shouldBe 1539
            }
        }
    }
}