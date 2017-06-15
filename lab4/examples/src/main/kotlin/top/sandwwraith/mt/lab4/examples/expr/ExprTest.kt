package top.sandwwraith.mt.lab4.examples.expr

fun main(args: Array<String>) {
    println(ExprParser(ExprLexer(args[0].reader())).parse())
}
