package top.sandwwraith.mt.lab4.internal

import top.sandwwraith.mt.lab4.GrammarBaseListener
import top.sandwwraith.mt.lab4.GrammarParser
import top.sandwwraith.mt.lab4.runtime.Token

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

internal class GrammarCollector : GrammarBaseListener() {
    private var tokenCnt = 0

    internal val _tokenTable: MutableMap<String, Token> = LinkedHashMap()
    internal val terms: Set<String>
        get() = if (hasEPSprods) _tokenTable.keys + EPS else _tokenTable.keys

    internal val _patterns: MutableMap<Token, Regex> = HashMap()
    internal val _literals: MutableMap<Token, String> = HashMap()
    internal val _tokensToSkip: MutableSet<Token> = HashSet()

    internal val _rules: MutableMap<String, Rule> = LinkedHashMap()

    private var hasEPSprods = false
    internal val nonTerms: Set<String>
        get() = _rules.keys

    internal var pckg: String? = null
    internal var members: String? = null
    internal lateinit var startNT: String

    // Misc

    override fun exitPckg(ctx: GrammarParser.PckgContext) {
        pckg = ctx.text.removePrefix("+package").cleanUpCode()
    }

    override fun exitMembers(ctx: GrammarParser.MembersContext) {
        members = ctx.text.removePrefix("+members").cleanUpCode()
    }

    private fun String.cleanUpCode() = this.trim('{', '}', ' ')

    // Parser

    override fun exitBegin(ctx: GrammarParser.BeginContext) {
        startNT = ctx.NT_ID().text
    }

    override fun exitParserRulee(ctx: GrammarParser.ParserRuleeContext) {
        val curRule = _rules.getOrPut(ctx.NT_ID().text, { Rule(ctx.NT_ID().text) })

        if (ctx.outAttr() != null)
            curRule.returnType = ctx.outAttr().text
        if (ctx.inAttrs() != null)
            curRule.args = ctx.inAttrs().param().map { it.paramName().text to it.paramType().text }

        curRule.productions = ctx.prods().map { prodsCtx ->
            val prod = if (prodsCtx.prod().isEmpty()) {
                hasEPSprods = true
                listOf(ProdElem.Term(EPS))
            } else
                prodsCtx.prod().map { prodCtx ->
                    if (prodCtx.NT_ID() != null) {
                        ProdElem.NonTerm(
                                prodCtx.NT_ID().text,
                                prodCtx.args()?.CODE()?.map { it.text.cleanUpCode() }
                        )
                    } else {
                        ProdElem.Term(prodCtx.T_ID().text)
                    }
                }
            val code = prodsCtx.CODE()?.text?.cleanUpCode()
            Production(prod, code)
        }
    }

    // Lexer

    override fun exitTokenRule(ctx: GrammarParser.TokenRuleContext) {
        fillMap(ctx.T_ID().text, ctx.term_value(), false)
    }

    override fun exitSkipRule(ctx: GrammarParser.SkipRuleContext) {
        fillMap(ctx.T_ID().text, ctx.term_value(), true)
    }

    override fun exitFile(ctx: GrammarParser.FileContext?) {
        _tokenTable.put(EOF, -1)
    }

    private fun fillMap(token: String, right: GrammarParser.Term_valueContext, skip: Boolean) {
        val token_id = _tokenTable.getOrPut(token, { tokenCnt++ })
        if (right.STRING() != null) {
            _literals.put(token_id, right.STRING().text.trim('\"'))
        } else {
            _patterns.put(token_id, Regex(right.REGEX().text.trim('\'')))
        }
        if (skip) _tokensToSkip.add(token_id)
    }
}