package top.sandwwraith.mt.lab4.examples.hello

fun main(args: Array<String>) {
    println(HelloParser(HelloLexer(args[0].reader())).parse())
}
