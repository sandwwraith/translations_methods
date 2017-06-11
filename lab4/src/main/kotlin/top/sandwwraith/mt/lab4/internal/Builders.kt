package top.sandwwraith.mt.lab4.internal

import top.sandwwraith.mt.lab4.runtime.Token

abstract class BuilderHelper {

    private var indent = 0

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

    protected fun scoped(block: () -> Unit) {
        indent++
        block()
        indent--
    }

    protected fun StringBuilder.pop() {
        var i = length - 1
        while (this[i].isWhitespace() && i >= 0) i--
        if (i >= 0) deleteCharAt(i)
    }
}

internal class ParserBuilder(val collector: GrammarCollector) {

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
}

internal class LexerBuilder(val collector: GrammarCollector) : BuilderHelper() {

    val tokensToSkip: Set<Token>
        get() = collector._tokensToSkip

    val literals: Map<Token, String>
        get() = collector._literals

    val patterns: Map<Token, Regex>
        get() = collector._patterns

    val tokenTable: Map<String, Token>
        get() = collector._tokenTable

    fun generateLexerData(grammarName: String) = buildString {
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