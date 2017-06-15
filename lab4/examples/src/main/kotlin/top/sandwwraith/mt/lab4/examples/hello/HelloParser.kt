package top.sandwwraith.mt.lab4.examples.hello

import top.sandwwraith.mt.lab4.runtime.ParsingException
import top.sandwwraith.mt.lab4.runtime.Token

@Suppress("UNUSED_VARIABLE")
class HelloParser(private val lexer: HelloLexer) {

    private fun skip(token: Token): String {
        if (lexer.token != token) throw ParsingException.expectedNotFound(lexer, token)
        val res = lexer.tokenValue ?: throw IllegalArgumentException("Cannot skip EOF token")
        lexer.next()
        return res
    }

    private fun Hello(): String = when (lexer.token) {
        TOKENS.HELLO -> {
            val HELLO = skip(TOKENS.HELLO)
            val a = A()
            ("Hello, " + a + "!").capitalize()
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.HELLO)
    }

    private fun A(): String = when (lexer.token) {
        TOKENS.ID -> {
            val ID = skip(TOKENS.ID)
            ID
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.ID)
    }

    fun parse(): String {
        lexer.next()
        return Hello()
    }
}
