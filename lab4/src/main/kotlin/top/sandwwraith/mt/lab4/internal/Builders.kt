package top.sandwwraith.mt.lab4.internal

import top.sandwwraith.mt.lab4.runtime.ParsingException
import top.sandwwraith.mt.lab4.runtime.Token

abstract class AbstractGrammarFilesGenerator {

    protected var indent = 0

    protected fun StringBuilder.l(line: String) {
        for (i in 0 until (4 * indent)) append(" ")
        append(line)
        append(System.lineSeparator())
    }

    protected val StringBuilder.nl: Unit
        get() {
            append(System.lineSeparator())
            Unit
        }

    protected inline fun scoped(block: () -> Unit) {
        indent++
        block()
        indent--
    }

    protected fun StringBuilder.pop() {
        var i = length - 1
        while (this[i].isWhitespace() && i >= 0) i--
        if (i >= 0) deleteCharAt(i)
    }

    abstract fun generate(grammarName: String): String
}

internal class TestGrammarFilesGenerator(val collector: GrammarCollector) : AbstractGrammarFilesGenerator() {
    override fun generate(grammarName: String) = buildString {
        val gn = grammarName.capitalize()
        collector.pckg?.let {
            l("package $it")
            nl
        }
        l("fun main(args: Array<String>) {")
        scoped {
            l("println(${gn}Parser(${gn}Lexer(args[0].reader())).parse())")
        }
        l("}")
    }

}

internal class ParserGrammarFilesGenerator(val collector: GrammarCollector) : AbstractGrammarFilesGenerator() {

    val first: Map<String, Set<String>> by lazy {
        val fst: MutableMap<String, MutableSet<String>> = mutableMapOf()

        collector.terms.forEach { fst.put(it, mutableSetOf(it)) }
        collector._rules.forEach { (name, rule) ->
            fst.put(name, mutableSetOf())
            if (rule.productions.any { it.first().name == EPS }) fst[name]!!.add(EPS)
        }

        var changed = true
        while (changed) {
            changed = false
            for ((name, rule) in collector._rules) {
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

        collector.nonTerms.forEach { flw.put(it, mutableSetOf()) }
        flw.getValue(collector.startNT).add(EOF)

        var changed = true
        while (changed) {
            changed = false
            for ((name, rule) in collector._rules) {
                rule.productions.forEach { prod ->
                    // For A -> aBb, add to FOLLOW(B) all from FIRST(b) except EPS
                    (0..prod.size - 2)
                            .filter { prod[it] is ProdElem.NonTerm }
                            .forEach { i ->
                                changed = changed || flw.getValue(prod[i].name).addAll(
                                        first.getValue(prod[i + 1].name).filter { it != EPS }
                                )
                            }

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

    override fun generate(grammarName: String) = buildString {
        val gn = grammarName.capitalize()
        collector.pckg?.let {
            l("package $it")
            nl
        }
        l("import top.sandwwraith.mt.lab4.runtime.Token")
        l("import top.sandwwraith.mt.lab4.runtime.ParsingException")
        nl
        l("class ${gn}Parser(private val lexer: ${gn}Lexer) {")
        nl
        scoped {
            l("private fun skip(token: Token): String {")
            scoped {
                l("if (lexer.token != token) throw ParsingException.expectedNotFound(lexer, token)")
                l("val res = lexer.tokenValue ?: throw IllegalArgumentException(\"Cannot skip EOF token\")")
                l("lexer.next()")
                l("return res")
            }
            l("}")
            nl
            for ((name, rule) in collector._rules) {
                l("private fun ${name.capitalize()}(${rule.getArgs()}) : ${rule.returnType ?: "Unit"} = when(lexer.token) {")
                val m = mapRules(name, rule)
                scoped { ->
                    for ((prod, tokens) in m) {
                        // Tokens
                        l("${tokens.map { "TOKENS.$it" }.joinToString()} -> {")
                        scoped {
                            // Declarations
                            val lst = listable(prod)
                            lst.forEach({ (e, _) ->
                                when (e) {
                                    in collector.terms -> l("val $e : MutableList<String> = mutableListOf()")
                                    in collector.nonTerms -> {
                                        val returnType = collector._rules[e]!!.returnType
                                        if (returnType != null)
                                            l("val $e : MutableList<$returnType> = mutableListOf()")
                                    }
                                }
                            })

                            // Assignments
                            for (elem in prod) {
                                when (elem) {
                                    is ProdElem.Term -> {
                                        if (elem.name in lst)
                                            l("${elem.name}.add(skip(TOKENS.${elem.name}))")
                                        else
                                            l("val ${elem.name} = skip(TOKENS.${elem.name})")
                                    }
                                    is ProdElem.NonTerm -> {
                                        val callAttrs = elem.callAttrs?.joinToString().orEmpty()
                                        if (elem.name in lst)
                                            l("${elem.name}.add(${elem.name.capitalize()}($callAttrs))")
                                        else
                                            l("val ${elem.name} = ${elem.name.capitalize()}($callAttrs)")
                                    }
                                }
                            }

                            // Return code
                            if (prod.code != null) l(prod.code)
                        }
                        l("}")
                    }

                    // else (error)
                    l("else -> throw ParsingException.expectedNotFound(lexer, " +
                            "${m.values.flatten().map { "TOKENS.$it" }.joinToString()})")
                }
                l("}")
                nl
            }
            val startRule = collector._rules.getValue(collector.startNT)

            l("fun parse(${startRule.getArgs()}) : ${startRule.returnType ?: "Unit"} { ")
            scoped {
                l("lexer.next()")
                l("return ${collector.startNT.capitalize()}(${startRule.args?.map { (a, _) -> a }?.joinToString().orEmpty()})")
            }
            l("}")
        }
        l("}")
    }

    private fun mapRules(name: String, rule: Rule) = rule.productions
            .associate { prod ->
                if (prod[0].name == EPS) prod to follow.getValue(name).toList()
                else prod to first.getValue(prod[0].name).toList()
            }
            .also {
                it.values.flatten().also {
                    if (it.size != it.distinct().size) throw ParsingException("It is not an LL(1) grammar!")
                }
            }

    private fun listable(prod: Production) =
            prod.asSequence().groupingBy { it.name }.eachCount().filterValues { i -> i > 1 }


    private fun Rule.getArgs() = args?.map { (n, t) -> "$n: $t" }?.joinToString().orEmpty()
}

internal class LexerGrammarFilesGenerator(val collector: GrammarCollector) : AbstractGrammarFilesGenerator() {

    val tokensToSkip: Set<Token>
        get() = collector._tokensToSkip

    val literals: Map<Token, String>
        get() = collector._literals

    val patterns: Map<Token, Regex>
        get() = collector._patterns

    val tokenTable: Map<String, Token>
        get() = collector._tokenTable

    override fun generate(grammarName: String) = buildString {
        collector.pckg?.let {
            l("package $it")
            nl
        }
        l("import java.io.Reader")
        l("import top.sandwwraith.mt.lab4.runtime.Token")
        l("import top.sandwwraith.mt.lab4.runtime.RuledLexer")
        nl
        l("private val _literals: Map<Token, String> = mapOf(")
        scoped { literals.forEach { t, s -> l("$t to \"${s.escape()}\",") } }
        pop()
        l(")")
        nl
        l("private val _patterns: Map<Token, Regex> = mapOf(")
        scoped { patterns.forEach { t, r -> l("$t to Regex(\"${r.toString().escape()}\"),") } }
        pop()
        l(")")
        nl
        l("private val _tokensToSkip = setOf(${tokensToSkip.joinToString()})")
        nl
        l("object TOKENS {")
        scoped { (tokenTable - tokensToSkip).forEach { t, i -> l("val $t = $i") } }
        l("}")
        nl
        l("class ${grammarName.capitalize()}Lexer(reader: Reader)")
        scoped { l(": RuledLexer(reader, _literals, _patterns, _tokensToSkip, TOKENS.EOF)") }
    }

    private fun String.escape() = replace("\\", "\\\\").replace("\"", "\\\"")

}