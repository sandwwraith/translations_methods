package top.sandwwraith.mt.lab4.examples.expr

import top.sandwwraith.mt.lab4.runtime.GroupMatcherLexer
import top.sandwwraith.mt.lab4.runtime.Token
import java.io.Reader

private val _literals: Map<Token, String> = mapOf(
        0 to "e",
        1 to "+",
        2 to "**",
        3 to "*",
        4 to "(",
        5 to ")"
)

private val _patterns: Map<Token, Regex> = mapOf(
        6 to Regex("[0-9]+"),
        7 to Regex("\\s+")
)

private val _tokensToSkip = setOf(7)

object TOKENS {
    val EXP = 0
    val PLUS = 1
    val DMUL = 2
    val MUL = 3
    val O = 4
    val C = 5
    val NUM = 6
    val WS = 7
    val EOF = -1
}

class ExprLexer(reader: Reader)
    : GroupMatcherLexer(reader, _literals, _patterns, _tokensToSkip, TOKENS.EOF)
