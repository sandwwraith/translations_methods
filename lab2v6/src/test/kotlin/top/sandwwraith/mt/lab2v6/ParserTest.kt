package top.sandwwraith.mt.lab2v6

import io.kotlintest.matchers.haveSize
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.ShouldSpec

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

class ParserTest : ShouldSpec() {
    init {
        val testString1 = "int a, **b, d;"
        "Parsing '$testString1'" {
            val tree = Parser(Lexer(testString1)).parse()
            should("have type of int") {
                val types = tree.takeTypes()
                types should haveSize(1)
                types[0] shouldBe "int"
            }
            /*should("have variables a, **b, d") {
                val vars = tree.extractVars()
                vars should haveSize(3)
                vars shouldBe listOf(0 to "a", 2 to "b", 0 to "d")
            }*/
        }

        val testString2 = "int a;char *c;\ndouble **x, **y;"
        "Parsing '$testString2'" {
            val tree = Parser(Lexer(testString2)).parse()
            should("have three types: int, char, double") {
                val types = tree.takeTypes()
                types shouldBe listOf("int", "char", "double")
            }
            /*should("have all variables") {
                val vars = tree.extractVars()
                vars should haveSize(4)
                vars shouldBe listOf(0 to "a", 1 to "c", 2 to "x", 2 to "y")
            }*/
        }

        "Parsing broken string" {
            should("throw an exception") {
                shouldThrow<ParsingException> { Parser(Lexer("int ke*k;")).parse() }
                shouldThrow<ParsingException> { Parser(Lexer("char;")).parse() }
                shouldThrow<ParsingException> { Parser(Lexer("char ***xxx")).parse() }
            }
        }

        val testString3 = "int &a, **b;double x;char ***C;"
        "Parsing '$testString3'" {
            val tree = Parser(Lexer(testString3)).parse()
            should("have three types: int, double, char") {
                val types = tree.takeTypes()
                types shouldBe listOf("int", "double", "char")
            }
            /*should("have all variables") {
                val vars = tree.extractVars()
                vars should haveSize(4)
                vars shouldBe listOf(1 to "a", 2 to "b", 0 to "x", 3 to "C")
            }*/
            should("not allow amps") {
                shouldThrow<ParsingException> { Parser(Lexer("int &&k;")).parse()  }
                shouldThrow<ParsingException> { Parser(Lexer("int &*k;")).parse()  }
                shouldThrow<ParsingException> { Parser(Lexer("int &;")).parse()  }
            }
        }
    }
}