package top.sandwwraith.mt.lab2v6

class Parser(private val lexer: Lexer) {

    private fun skip(token: Token): TreeNode {
        if (lexer.token != token) throw ParsingException.expectedNotFound(lexer, token)
        val lastId = lexer.lastId
        val res = TreeNode(if (token == Token.ID && lastId != null) lastId else token.toString(), listOf(), term = true)
        lexer.next()
        return res
    }

    private fun N() = TreeNode("N", skip(Token.ID))

    private fun X(): TreeNode = when(lexer.token) {
        Token.ASTERISK -> TreeNode("X", skip(Token.ASTERISK), X())
        Token.ID -> TreeNode("X", N())
        else -> throw ParsingException.expectedNotFound(lexer, Token.ID, Token.ASTERISK)
    }

    private fun V(): TreeNode = when (lexer.token) {
        Token.ASTERISK -> TreeNode("V", skip(Token.ASTERISK), X())
        Token.ID -> TreeNode("V", N())
        Token.AMP -> TreeNode("V", skip(Token.AMP), N())
        else -> throw ParsingException.expectedNotFound(lexer, Token.ID, Token.ASTERISK, Token.AMP)
    }

    private fun Ws(): TreeNode = when (lexer.token) {
        Token.COMMA -> TreeNode("W'", skip(Token.COMMA), V(), Ws())
        Token.SEMICOLON -> TreeNode("W'")
        else -> throw ParsingException.expectedNotFound(lexer, Token.COMMA, Token.SEMICOLON)
    }

    private fun W() = when (lexer.token) {
        Token.ASTERISK, Token.ID, Token.AMP -> TreeNode("W", V(), Ws())
        else -> throw ParsingException.expectedNotFound(lexer, Token.ID, Token.ASTERISK, Token.AMP)
    }

    private fun T() = TreeNode("T", skip(Token.ID))

    private fun D() = when (lexer.token) {
        Token.ID -> TreeNode("D", T(), W(), skip(Token.SEMICOLON))
        else -> throw ParsingException.expectedNotFound(lexer, Token.ID)
    }

    private fun Ss(): TreeNode = when (lexer.token) {
        Token.ID -> TreeNode("S'", D(), Ss())
        Token.EOF -> TreeNode("S'")
        else -> throw ParsingException.expectedNotFound(lexer, Token.ID, Token.EOF)
    }

    private fun S() = when (lexer.token) {
        Token.ID -> TreeNode("S", D(), Ss())
        else -> throw ParsingException.expectedNotFound(lexer, Token.ID)
    }

    fun parse(): TreeNode {
        lexer.next()
        return S()
    }

    companion object {
        fun parse(s: String) = Parser(Lexer(s)).parse()
    }
}