package top.sandwwraith.mt.lab2v6

import io.kotlintest.matchers.Matcher
import io.kotlintest.matchers.Result
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.properties.forAll
import io.kotlintest.properties.headers
import io.kotlintest.properties.row
import io.kotlintest.properties.table
import io.kotlintest.specs.StringSpec

private fun Lexer.consume() {
    do {
        this.next()
    } while (this.token != Token.EOF)
}

private fun haveTokens(tokens: List<Token>) = object : Matcher<Lexer> {
    override fun test(value: Lexer): Result {
        for (t in tokens) {
            value.next()
            if (value.token != t) return Result(false, "Expected: $t, found: ${value.token}")
        }
        return Result(true, "")
    }
}

private fun haveLastId(id: String) = object : Matcher<Lexer> {
    override fun test(value: Lexer): Result {
        value.consume()
        return if (value.lastId == id)
            Result(true, "")
        else
            Result(false, "Expected: $id, found: ${value.lastId}")
    }
}

class LexerTest : StringSpec() {
    init {
        "Parse" {
            val mTable = table(
                    headers("input", "tokens"),
                    row("int", listOf(Token.ID)),
                    row("  int    ", listOf(Token.ID)),
                    row("int a", listOf(Token.ID, Token.ID)),
                    row("  int   a;", listOf(Token.ID, Token.ID, Token.SEMICOLON)),
                    row("char *c;", listOf(Token.ID, Token.ASTERISK, Token.ID, Token.SEMICOLON)),
                    row("double a, **b;", listOf(Token.ID, Token.ID, Token.COMMA, Token.ASTERISK, Token.ASTERISK, Token.ID, Token.SEMICOLON)),
                    row("", listOf())
            )
            forAll(mTable) { input, tokens ->
                Lexer(input) should haveTokens(tokens + Token.EOF)
            }
        }

        "Retain last identifier" {
            Lexer("int") should haveLastId("int")
            Lexer("double c;") should haveLastId("c")
        }

        "Throw on illegal characters" {
            shouldThrow<ParsingException> {
                Lexer("  ab ^!!:").consume()
            }
        }
    }
}