@file:JvmName("App")
package top.sandwwraith.mt.lab2v6

import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

fun helloWorldGenerator() = "Hello, world!"

fun main(args: Array<String>) {
    val t = Parser.parse("int *a;")
    Files.newBufferedWriter(Paths.get("kek.gv")).use {
        TreePrinter(t).printTo(it)
    }
}
