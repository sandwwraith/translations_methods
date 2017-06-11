@file:JvmName("App")

package top.sandwwraith.mt.lab4

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import top.sandwwraith.mt.lab4.internal.GrammarCollector
import top.sandwwraith.mt.lab4.internal.LexerGrammarFilesGenerator
import top.sandwwraith.mt.lab4.internal.ParserGrammarFilesGenerator
import top.sandwwraith.mt.lab4.internal.TestGrammarFilesGenerator
import java.nio.file.Files
import java.nio.file.Paths

private fun collect(stream: CharStream): GrammarCollector {
    val collector: GrammarCollector = GrammarCollector()
    val lexer = GrammarLexer(stream)
    val parser = GrammarParser(CommonTokenStream(lexer))
    val walker = ParseTreeWalker()
    walker.walk(collector, parser.file())
    return collector
}

private fun genAll(grammarName: String, targetDir: String, collector: GrammarCollector) {
    val lex = LexerGrammarFilesGenerator(collector) to "Lexer"
    val par = ParserGrammarFilesGenerator(collector) to "Parser"
    val test = TestGrammarFilesGenerator(collector) to "Test"

    listOf(lex, par, test).forEach { (gen, postfix) ->
        val out = Paths.get(targetDir, "$grammarName$postfix.kt")
        out.toFile().parentFile.mkdirs()
        Files.newBufferedWriter(out).use { wr ->
            wr.write(gen.generate(grammarName))
        }
    }
}

fun main(args: Array<String>) {
    val inputFile = args[0]
    val targetDir = args[1]

    val grammarName = inputFile.removeSuffix(".gram").capitalize()
    val collector = collect(CharStreams.fromFileName(inputFile))

    genAll(grammarName, targetDir, collector)
}