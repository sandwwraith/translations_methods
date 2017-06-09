package top.sandwwraith.mt.lab4.runtime

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/
import java.io.IOException
import java.io.PushbackReader
import java.io.Reader

typealias Token = Int

open class RuledLexer(_reader: Reader,
                      literals: Map<Token, String>,
                      patterns: Map<Token, Regex>,
                      private val tokensToSkip: Set<Token>,
                      private val EOF_TOKEN: Token = -1) {

    private val tokens: Map<Token, Regex> = patterns + literals.mapValues { (_, v) -> Regex.fromLiteral(v) }
    private val reader = PushbackReader(_reader)

    var token: Token = EOF_TOKEN
        private set

    var tokenValue: String? = null
        private set

    private var cur: Int = -1

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

    private fun unread() {
        position--
        try {
            reader.unread(cur)
        } catch (e: IOException) {
            throw ParsingException(e.message ?: "IO error", position)
        }
    }

    private fun _next() {
        read()

        if (cur == -1) {
            token = EOF_TOKEN
            tokenValue = null
            return
        }

        val builder = StringBuilder()
        val possible = tokens.keys.toMutableSet()
        var first = true

        while (possible.isNotEmpty()) {
            token = possible.first()
            if (cur == -1) break

            builder.append(cur.toChar())
            val unMatched = possible.filter { !tokens.getValue(it).matches(builder) }
            val nomatch = unMatched.size == possible.size
            if (first && nomatch) throw ParsingException("Unexpected symbol ${cur.toChar()}", position)
            first = false

            if (!nomatch) {
                read()
            } else {
                builder.deleteCharAt(builder.length - 1)
                unread()

                if (possible.size > 1) throw ParsingException("Ambiguous tokens, all patterns match: " +
                        "${possible.map { tokens.getValue(it) }}", position)
            }
            possible.removeAll(unMatched)
        }

        tokenValue = builder.toString()
    }

    fun next() {
        do {
            _next()
        } while (token in tokensToSkip)
    }

    val nextToken: Pair<Token, String?>
        get() {
            next()
            return token to tokenValue
        }
}