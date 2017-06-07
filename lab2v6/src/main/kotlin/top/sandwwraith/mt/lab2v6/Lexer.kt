package top.sandwwraith.mt.lab2v6

import java.io.IOException
import java.io.Reader
import java.io.StringReader

class Lexer(private val reader: Reader) {

    constructor(line: String) : this(StringReader(line))

    lateinit var token: Token
        private set

    var lastId: String? = null
        private set

    private var cur: Int = ' '.toInt()

    var position: Int = -1
        private set

    private fun read() {
        position++
        try {
            cur = reader.read()
        } catch (e: IOException) {
            throw ParsingException(e.message ?: "IO error", position)
        }
    }

    private fun Int.isBlank() = Character.isWhitespace(this)

    private fun Int.isIdChar(): Boolean = 'a'.toInt() <= this && this <= 'z'.toInt()
            || 'A'.toInt() <= this && this <= 'Z'.toInt()
            || '0'.toInt() <= this && this <= '9'.toInt()

    fun next() {
        while (cur.isBlank()) read()

        var isId = false
        val builder = StringBuilder()
        while (cur.isIdChar()) {
            isId = true
            builder.append(cur.toChar())
            read()
        }

        if (isId) {
            lastId = builder.toString()
            token = Token.ID
            return
        }

        when (cur) {
            ','.toInt() -> {
                token = Token.COMMA
                read()
            }

            ';'.toInt() -> {
                token = Token.SEMICOLON
                read()
            }

            '*'.toInt() -> {
                token = Token.ASTERISK
                read()
            }

            '&'.toInt() -> {
                token = Token.AMP
                read()
            }

            -1 -> {
                token = Token.EOF
            }

            else -> throw ParsingException("Illegal character", position)
        }
    }


}