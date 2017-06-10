package top.sandwwraith.mt.lab4.runtime

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import top.sandwwraith.mt.lab4.GrammarBaseListener
import top.sandwwraith.mt.lab4.GrammarLexer
import top.sandwwraith.mt.lab4.GrammarParser

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

private class LexerVisitor : GrammarBaseListener() {
    private var tokenCnt = 0

    internal val _tokenTable: MutableMap<String, Token> = LinkedHashMap()
    internal val _patterns: MutableMap<Token, Regex> = HashMap()
    internal val _literals: MutableMap<Token, String> = HashMap()
    internal val _tokenToSkip: MutableSet<Token> = HashSet()

    override fun exitTokenRule(ctx: GrammarParser.TokenRuleContext) {
        fillMap(ctx.T_ID().text, ctx.term_value(), false)
    }

    override fun exitSkipRule(ctx: GrammarParser.SkipRuleContext) {
        fillMap(ctx.T_ID().text, ctx.term_value(), true)
    }

    private fun fillMap(token: String, right: GrammarParser.Term_valueContext, skip: Boolean) {
        val token_id = _tokenTable.getOrPut(token, { tokenCnt++ })
        if (right.STRING() != null) {
            _literals.put(token_id, right.STRING().text.trim('\"'))
        } else {
            _patterns.put(token_id, Regex(right.REGEX().text.trim('\'')))
        }
        if (skip) _tokenToSkip.add(token_id)
    }
}

class LexerBuilder(stream: CharStream) {
    private val visitor: LexerVisitor = LexerVisitor()

    init {
        val lexer = GrammarLexer(stream)
        val parser = GrammarParser(CommonTokenStream(lexer))
        val walker = ParseTreeWalker()
        walker.walk(visitor, parser.file())
    }

    val tokenToSkip: Set<Token>
        get() = visitor._tokenToSkip

    val literals: Map<Token, String>
        get() = visitor._literals

    val patterns: Map<Token, Regex>
        get() = visitor._patterns

    val tokenTable: Map<String, Token>
        get() = visitor._tokenTable

}