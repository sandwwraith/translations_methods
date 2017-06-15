package top.sandwwraith.mt.lab4.examples.hello

import top.sandwwraith.mt.lab4.runtime.GroupMatcherLexer
import top.sandwwraith.mt.lab4.runtime.Token
import java.io.Reader

private val _literals: Map<Token, String> = mapOf(
        0 to "hello"
)

private val _patterns: Map<Token, Regex> = mapOf(
        1 to Regex("[A-Z][a-z]*"),
        2 to Regex("\\s+")
)

private val _tokensToSkip = setOf(2)

object TOKENS {
    val HELLO = 0
    val ID = 1
    val WS = 2
    val EOF = -1
}

class HelloLexer(reader: Reader)
    : GroupMatcherLexer(reader, _literals, _patterns, _tokensToSkip, TOKENS.EOF)
