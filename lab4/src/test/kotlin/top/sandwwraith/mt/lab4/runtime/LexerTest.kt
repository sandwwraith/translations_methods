package top.sandwwraith.mt.lab4.runtime

import io.kotlintest.matchers.Matcher
import io.kotlintest.matchers.Result
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import top.sandwwraith.mt.lab4.GrammarLexer
import top.sandwwraith.mt.lab4.GrammarParser
import top.sandwwraith.mt.lab4.internal.GrammarCollector
import top.sandwwraith.mt.lab4.internal.LexerGrammarFilesGenerator
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

private fun visitGrammar(gram: String): GrammarCollector {
    val collector: GrammarCollector = GrammarCollector()
    val lexer = GrammarLexer(CharStreams.fromString(gram))
    val parser = GrammarParser(CommonTokenStream(lexer))
    val walker = ParseTreeWalker()
    walker.walk(collector, parser.file())
    return collector
}


class LexerTest : ShouldSpec() {
    init {
        "Lexer for IDs and semicolons" {
            val gram = """|>begin WS => '\s+'; ID = '[a-z]+'; SC = ";"; """
            val patterns = mapOf(0 to Regex("\\s+"), 1 to Regex("[a-z]+"))
            val literals = mapOf(2 to ";")
            val tokensToSkip = setOf(0)

            val builder = LexerGrammarFilesGenerator(visitGrammar(gram))
            should("build correct tables") {
                builder.patterns.mapValues { (_, v) -> v.toString() } shouldEqual patterns.mapValues { (_, v) -> v.toString() }
                builder.literals shouldEqual literals
                builder.tokensToSkip shouldEqual tokensToSkip
            }
            val lexer = RuledLexer(StringReader("int a;"), literals, patterns, tokensToSkip)
            should("tokenize 'int a;'") {
                lexer should haveTokens(1, 1, 2, -1)
            }
            should("get literal values from 'int a;'") {
                lexer should haveText("int", "a", ";", null)
            }
        }
    }
}