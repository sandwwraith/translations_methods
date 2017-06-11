package top.sandwwraith.mt.lab4.internal

import top.sandwwraith.mt.lab4.runtime.Token

internal class ParserBuilder(val visitor: GrammarVisitor) {

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

internal class LexerBuilder(val visitor: GrammarVisitor) {

    val tokensToSkip: Set<Token>
        get() = visitor._tokensToSkip

    val literals: Map<Token, String>
        get() = visitor._literals

    val patterns: Map<Token, Regex>
        get() = visitor._patterns

    val tokenTable: Map<String, Token>
        get() = visitor._tokenTable

}