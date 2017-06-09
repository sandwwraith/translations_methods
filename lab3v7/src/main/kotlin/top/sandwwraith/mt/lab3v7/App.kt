@file:JvmName("App")

package top.sandwwraith.mt.lab3v7

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import top.sandwwraith.mt.lab3v7.Constants.FILE_EXT
import java.nio.file.Files
import java.nio.file.Paths

sealed class TranslateResult {
    class Success(val result: String) : TranslateResult()

    class Error(val errors: List<String>) : TranslateResult()
}

private fun translate(stream: CharStream): TranslateResult {
    val collector = ErrorCollector()
    val lexer = RatNumsLexer(stream).apply {
        removeErrorListeners()
        addErrorListener(collector)
    }

    val parser = RatNumsParser(CommonTokenStream(lexer)).apply {
        removeErrorListeners()
        addErrorListener(collector)
    }

    val trans = RatNumTranslator()

    try {
        val result = trans.visit(parser.program())
        if (collector.hasErrors())
            return TranslateResult.Error(collector.getErrors())
        else
            return TranslateResult.Success(result)
    } catch (e: Exception) {
        e.message?.let { collector.addError(it) }
        return TranslateResult.Error(collector.getErrors())
    }
}

fun executeGcc(name: String, out: String) =
        ProcessBuilder("gcc", "$name.c", "-lgmp", "-o", out)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor()

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("You should specify input file and optional output file as command line arguments.")
        return
    }


    // Translate
    val fName = args[0]
    val i = fName.indexOf(FILE_EXT)
    if (i == -1 || !fName.endsWith(FILE_EXT)) {
        System.err.println("$fName not an $FILE_EXT file, exiting.")
        return
    }
    val fout = fName.substring(0, i)
    val res = translate(CharStreams.fromFileName(fName, Charsets.UTF_8))
    when (res) {
        is TranslateResult.Success -> Files.newBufferedWriter(Paths.get("$fout.c"), Charsets.UTF_8)
                .use { it.write(res.result) }
        is TranslateResult.Error -> {
            val prefix = "Following errors occurred during compilation of $fName:\n"
            System.err.println(res.errors.joinToString(prefix = prefix, separator = "\n"))
        }
    }

    //Compile
    if (args.size < 2) return
    val pName = args[1]
    executeGcc(fout, pName)
}