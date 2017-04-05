@file:JvmName("App")

package top.sandwwraith.mt.lab2v6

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

fun executeDot(name: String) =
        ProcessBuilder("dot", "-Tsvg", "$name.gv")
                .redirectOutput(File("$name.svg"))
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor()

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println("Please specify input string as command-line argument and optional output name as second argument")
        return
    }
    val name: String? = if (args.size == 1) null else args[1]

    val tree = Parser.parse(args[0])
    if (name != null) {
        Files.newBufferedWriter(Paths.get("$name.gv")).use {
            TreePrinter(tree, name).printTo(it)
        }
        val code = executeDot(name)
        if (code != 0) System.err.println("Dot has non-zero exit status, maybe something went wrong")
    } else {
        System.out.bufferedWriter().use {
            TreePrinter(tree).printTo(it)
        }
    }
}
