package top.sandwwraith.mt.lab4

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import top.sandwwraith.mt.lab4.runtime.Token

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

internal data class Rule(val name: String,
                         var productions: List<Production> = ArrayList(),
                         var returnType: String? = null,
                         var args: List<Pair<String, String>>? = null)

internal data class Production(val prods: List<ProdElem>, val code: String? = null) : List<ProdElem> by prods

internal const val EPS = "EPS"
internal const val EOF = "EOF"

internal sealed class ProdElem {
    abstract val name: String

    data class Term(override val name: String) : ProdElem()

    data class NonTerm(override val name: String, val callAttrs: List<String>?) : ProdElem()
}

internal class LexerVisitor : GrammarBaseListener() {
    private var tokenCnt = 0

    internal val _tokenTable: MutableMap<String, Token> = LinkedHashMap()
    internal val terms: Set<String>
        get() = if (hasEPSprods) _tokenTable.keys + EPS else _tokenTable.keys

    internal val _patterns: MutableMap<Token, Regex> = HashMap()
    internal val _literals: MutableMap<Token, String> = HashMap()
    internal val _tokenToSkip: MutableSet<Token> = HashSet()

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
        if (skip) _tokenToSkip.add(token_id)
    }
}

internal class ParserBuilder(val visitor: LexerVisitor) {

    val first: Map<String, Set<String>> by lazy {
        val fst: MutableMap<String, MutableSet<String>> = mutableMapOf()

        visitor.terms.forEach { fst.put(it, mutableSetOf(it)) }
        visitor._rules.forEach { (name, rule) ->
            fst.put(name, mutableSetOf())
            if (rule.productions.any { it.first().name == EPS }) fst[name]!!.add(EPS)
        }

        var changed = true
        while (changed) {
            changed = false
            for ((name, rule) in visitor._rules) {
                for (prod in rule.productions) {
                    for (i in prod.indices) {
                        val curNT = prod[i].name
                        if (EPS in fst.getValue(curNT)) {
                            changed = changed || fst.getValue(name).addAll(fst.getValue(curNT))
                            if (i == prod.size - 1) changed = changed || fst.getValue(name).add(EPS)
                        } else {
                            changed = changed || fst.getValue(name).addAll(fst.getValue(curNT))
                            break
                        }
                    }
                }
            }
        }
        fst
    }

    val follow: Map<String, Set<String>> by lazy {
        val flw: MutableMap<String, MutableSet<String>> = mutableMapOf()
        visitor.nonTerms.forEach { flw.put(it, mutableSetOf()) }
        flw.getValue(visitor.startNT).add(EOF)
        var changed = true
        while (changed) {
            changed = false
            for ((name, rule) in visitor._rules) {
                rule.productions.forEach { prod ->
                    // For A -> aBb, add to FOLLOW(B) all from FIRST(b) except EPS
                    (0..prod.size - 2)
                            .filter { prod[it] is ProdElem.NonTerm }
                            .forEach { i -> changed = changed || flw.getValue(prod[i].name).addAll(first.getValue(prod[i + 1].name).filter { it != EPS }) }

                    // For A -> aB, add to FOLLOW(B) all from FOLLOW(A)
                    if (prod.last() is ProdElem.NonTerm)
                        changed = changed || flw.getValue(prod.last().name).addAll(flw.getValue(name))

                    // For A -> aBb, FIRST(b) has EPS, add to FOLLOW(B) all from FOLLOW(A)
                    if (prod.size > 1 && EPS in first.getValue(prod.last().name)) {
                        val prelast = prod[prod.size - 2]
                        if (prelast is ProdElem.NonTerm)
                            changed = changed || flw.getValue(prelast.name).addAll(flw.getValue(name))
                    }
                }
            }
        }
        flw
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