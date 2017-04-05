package top.sandwwraith.mt.lab2v6

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

data class TreeNode(val name: String, val children: List<TreeNode>, val term: Boolean = false) {
    constructor(name: String, vararg children: TreeNode) : this(name, children.asList())
}

fun TreeNode.takeTypes(): List<String> = when (this.name) {
    "S" -> listOf(children[0].children[0].children[0].name) + children[1].takeTypes()
    "S'" -> {
        if (children.isNotEmpty()) listOf(children[0].children[0].children[0].name) + children[1].takeTypes() else listOf<String>()
    }
    else -> throw IllegalArgumentException()
}

fun TreeNode.extractVars(): List<Pair<Int, String>> = when (this.name) {
    "V" -> {
        var depth = 0
        var c = this
        while (c.children.size == 2) {
            depth++
            c = c.children[1]
        }
        listOf(depth to c.children[0].children[0].name)
    }
    else -> children.flatMap { it.extractVars() }
}

class Parser(private val lexer: Lexer) {

    private fun skip(token: Token): TreeNode {
        if (lexer.token != token) throw ParsingException.createFromExpected(lexer, token)
        val lastId = lexer.lastId
        val res = TreeNode(if (token == Token.ID && lastId != null) lastId else token.toString(), listOf(), term = true)
        lexer.next()
        return res
    }

    private fun N() = TreeNode("N", skip(Token.ID))

    private fun V(): TreeNode = when (lexer.token) {
        Token.ASTERISK -> TreeNode("V", skip(Token.ASTERISK), V())
        Token.ID -> TreeNode("V", N())
        else -> throw ParsingException.createFromExpected(lexer, Token.ID, Token.ASTERISK)
    }

    private fun Ws(): TreeNode = when (lexer.token) {
        Token.COMMA -> TreeNode("W'", skip(Token.COMMA), V(), Ws())
        Token.SEMICOLON -> TreeNode("W'")
        else -> throw ParsingException.createFromExpected(lexer, Token.COMMA, Token.SEMICOLON)
    }

    private fun W() = when (lexer.token) {
        Token.ASTERISK, Token.ID -> TreeNode("W", V(), Ws())
        else -> throw ParsingException.createFromExpected(lexer, Token.ID, Token.ASTERISK)
    }

    private fun T() = TreeNode("T", skip(Token.ID))

    private fun D() = when (lexer.token) {
        Token.ID -> TreeNode("D", T(), W(), skip(Token.SEMICOLON))
        else -> throw ParsingException.createFromExpected(lexer, Token.ID)
    }

    private fun Ss(): TreeNode = when (lexer.token) {
        Token.ID -> TreeNode("S'", D(), Ss())
        Token.EOF -> TreeNode("S'")
        else -> throw ParsingException.createFromExpected(lexer, Token.ID, Token.EOF)
    }

    private fun S() = when (lexer.token) {
        Token.ID -> TreeNode("S", D(), Ss())
        else -> throw ParsingException.createFromExpected(lexer, Token.ID)
    }

    fun parse(): TreeNode {
        lexer.next()
        return S()
    }

    companion object {
        fun parse(s: String) = Parser(Lexer(s)).parse()
    }
}