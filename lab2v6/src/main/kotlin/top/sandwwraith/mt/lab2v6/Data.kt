package top.sandwwraith.mt.lab2v6

enum class Token {
    ID, COMMA, SEMICOLON, ASTERISK, AMP, EOF;

    override fun toString() = when (this) {
        ID -> "<identifier>"
        COMMA -> ","
        SEMICOLON -> ";"
        ASTERISK -> "*"
        AMP -> "&"
        EOF -> "EOF"
    }
}

data class TreeNode(val name: String, val children: List<TreeNode>, val term: Boolean = false) {
    constructor(name: String, vararg children: TreeNode) : this(name, children.asList())

    fun takeTypes(): List<String> = when (this.name) {
        "S" -> listOf(children[0].children[0].children[0].name) + children[1].takeTypes()
        "S'" -> {
            if (children.isNotEmpty()) listOf(children[0].children[0].children[0].name) + children[1].takeTypes() else listOf<String>()
        }
        else -> throw IllegalArgumentException()
    }

    fun extractVars(): List<Pair<Int, String>> = when (this.name) {
        "V" -> {
            if (children.size == 1) listOf(0 to children[0].children[0].name)
            else if (children.size == 2 && children[0].name == "&") listOf(-1 to children[1].children[0].name)
            else children[1].extractVars()
        }
        "X" -> {
            var depth = 1
            var c = this
            while (c.children.size == 2) {
                depth++
                c = c.children[1]
            }
            listOf(depth to c.children[0].children[0].name)
        }
        else -> children.flatMap { it.extractVars() }
    }
}
