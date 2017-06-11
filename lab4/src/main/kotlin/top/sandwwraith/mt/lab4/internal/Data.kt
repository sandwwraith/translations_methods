package top.sandwwraith.mt.lab4.internal

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

internal const val EPS = "EPS"
internal const val EOF = "EOF"

internal data class Rule(
        val name: String,
        var productions: List<Production> = ArrayList(),
        var returnType: String? = null,
        var args: List<Pair<String, String>>? = null
)

internal data class Production(val prods: List<ProdElem>, val code: String? = null) : List<ProdElem> by prods

internal sealed class ProdElem {
    abstract val name: String

    data class Term(override val name: String) : ProdElem()
    data class NonTerm(override val name: String, val callAttrs: List<String>?) : ProdElem()
}