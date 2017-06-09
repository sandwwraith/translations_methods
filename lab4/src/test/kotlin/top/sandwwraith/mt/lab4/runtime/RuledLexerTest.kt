package top.sandwwraith.mt.lab4.runtime

import io.kotlintest.matchers.Matcher
import io.kotlintest.matchers.Result
import io.kotlintest.matchers.should
import io.kotlintest.specs.ShouldSpec
import java.io.StringReader

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

typealias Lexer = RuledLexer

private fun haveTokens(vararg tokens: Token) = haveTokens(tokens.toList())

private fun haveTokens(tokens: List<Token>) = object : Matcher<Lexer> {
    override fun test(value: Lexer): Result {
        for (t in tokens) {
            value.next()
            if (value.token != t) return Result(false, "Expected: $t, found: ${value.token}")
        }
        return Result(true, "")
    }
}

private fun haveText(vararg text: String?) = haveText(text.toList())

private fun haveText(text: List<String?>) = object : Matcher<Lexer> {
    override fun test(value: Lexer): Result {
        for (t in text) {
            value.next()
            if (value.tokenValue != t) return Result(false, "Expected: $t, found: ${value.token}")
        }
        return Result(true, "")
    }
}


class RuledLexerTest : ShouldSpec() {
    init {
        "Lexer for IDs and semicolons" {
            val patterns = mapOf(0 to Regex("\\s+"), 1 to Regex("[a-z]+"))
            val literals = mapOf(2 to ";")
            val tokensToSkip = setOf(0)
            val tokenize: (String) -> RuledLexer = { s -> RuledLexer(StringReader(s), literals, patterns, tokensToSkip) }
            should("Parse 'int a;'") {
                tokenize("int a;") should haveTokens(1, 1, 2, -1)
                tokenize("int a; ") should haveText("int", "a", ";", null)
            }
        }
    }
}