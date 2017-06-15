package top.sandwwraith.mt.lab4.examples.expr

import top.sandwwraith.mt.lab4.runtime.ParsingException
import top.sandwwraith.mt.lab4.runtime.Token

@Suppress("UNUSED_VARIABLE")
class ExprParser(private val lexer: ExprLexer) {

    private fun skip(token: Token): String {
        if (lexer.token != token) throw ParsingException.expectedNotFound(lexer, token)
        val res = lexer.tokenValue ?: throw IllegalArgumentException("Cannot skip EOF token")
        lexer.next()
        return res
    }

    private fun Expr(): Int = when (lexer.token) {
        TOKENS.O, TOKENS.NUM -> {
            val term = Term()
            val exprs = Exprs(term)
            exprs
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.O, TOKENS.NUM)
    }

    private fun Exprs(acc: Int): Int = when (lexer.token) {
        TOKENS.PLUS -> {
            val PLUS = skip(TOKENS.PLUS)
            val term = Term()
            val next = acc + term
            val exprs = Exprs(next)
            exprs
        }
        TOKENS.EOF, TOKENS.C -> {
            acc
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.PLUS, TOKENS.EOF, TOKENS.C)
    }

    private fun Term(): Int = when (lexer.token) {
        TOKENS.O, TOKENS.NUM -> {
            val factor = Factor()
            val terms = Terms(factor)
            terms
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.O, TOKENS.NUM)
    }

    private fun Terms(acc: Int): Int = when (lexer.token) {
        TOKENS.MUL -> {
            val MUL = skip(TOKENS.MUL)
            val factor = Factor()
            val terms = Terms(acc * factor)
            terms
        }
        TOKENS.PLUS, TOKENS.EOF, TOKENS.C -> {
            acc
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.MUL, TOKENS.PLUS, TOKENS.EOF, TOKENS.C)
    }

    private fun Factor(): Int = when (lexer.token) {
        TOKENS.O, TOKENS.NUM -> {
            val single = Single()
            val factors = Factors(single)
            factors
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.O, TOKENS.NUM)
    }

    private fun Factors(acc: Int): Int = when (lexer.token) {
        TOKENS.DMUL -> {
            val DMUL = skip(TOKENS.DMUL)
            val single = Single()
            val factors = Factors(Math.pow(single.toDouble(), acc.toDouble()).toInt())
            factors
        }
        TOKENS.MUL, TOKENS.PLUS, TOKENS.EOF, TOKENS.C -> {
            acc
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.DMUL, TOKENS.MUL, TOKENS.PLUS, TOKENS.EOF, TOKENS.C)
    }

    private fun Single(): Int = when (lexer.token) {
        TOKENS.O -> {
            val O = skip(TOKENS.O)
            val expr = Expr()
            val C = skip(TOKENS.C)
            expr
        }
        TOKENS.NUM -> {
            val num = Num()
            num
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.O, TOKENS.NUM)
    }

    private fun Num(): Int = when (lexer.token) {
        TOKENS.NUM -> {
            val NUM = skip(TOKENS.NUM)
            val numExp = NumExp(NUM.toInt())
            numExp
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.NUM)
    }

    private fun NumExp(base: Int): Int = when (lexer.token) {
        TOKENS.EXP -> {
            val EXP = skip(TOKENS.EXP)
            val NUM = skip(TOKENS.NUM)
            Math.pow(base.toDouble(), NUM.toDouble()).toInt()
        }
        TOKENS.DMUL, TOKENS.MUL, TOKENS.PLUS, TOKENS.EOF, TOKENS.C -> {
            base
        }
        else -> throw ParsingException.expectedNotFound(lexer, TOKENS.EXP, TOKENS.DMUL, TOKENS.MUL, TOKENS.PLUS, TOKENS.EOF, TOKENS.C)
    }

    fun parse(): Int {
        lexer.next()
        return Expr()
    }
}
