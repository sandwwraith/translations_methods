package top.sandwwraith.mt.lab4.runtime

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/
import java.io.IOException
import java.io.Reader

typealias Token = Int

open class GroupMatcherLexer(_reader: Reader,
                             literals: Map<Token, String>,
                             patterns: Map<Token, Regex>,
                             private val tokensToSkip: Set<Token>,
                             private val EOF_TOKEN: Token = -1) {

    private val UNKNOWN_CHAR = -2

    private val allTokens: Map<Token, Regex> = literals.mapValues { (_, v) -> Regex.escape(v).toRegex() } + patterns + (UNKNOWN_CHAR to Regex(".+"))
    private val groupsToTokens: MutableMap<Int, Token> = LinkedHashMap()

    private val tokenStream: Iterator<TokenMatch>


    var token: Token = EOF_TOKEN
        private set

    var tokenValue: String? = null
        private set

    var position: Int = -1
        private set

    init {
        val text = _reader.readText()
        var grp = 0

        tokenStream = allTokens
                .map { (t, r) ->
                    groupsToTokens[grp++] = t
                    "($r)"
                }.joinToString("|")
                .toRegex().findAll(text)
                .map {
                    it.groups.mapIndexedNotNull { i, g ->
                        if (i == 0 || g == null) null else TokenMatch(i, g.range.start, g.value)
                    }.singleOrNull() ?: throw ParsingException("Ambiguous tokens")
                }
                .iterator()
    }

    data class TokenMatch(val groupPos: Int, val strPos: Int, val value: String? = null)

    fun _next() {
        if (!tokenStream.hasNext()) {
            if (token != EOF_TOKEN) {
                token = EOF_TOKEN
                tokenValue = null
                return
            } else throw IOException("No more tokens!")
        }
        val (g, s, v) = tokenStream.next()
        token = groupsToTokens.getValue(g - 1)
        position = s
        tokenValue = v
        if (token == UNKNOWN_CHAR) throw ParsingException("Unexpected symbol $v", position)
    }

    fun next() {
        do {
            _next()
        } while (token in tokensToSkip)
    }
}