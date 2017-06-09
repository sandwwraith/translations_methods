package top.sandwwraith.mt.lab3v7

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

object Constants {
    const val MPQ_TYPE = "mpq_t"
    const val RETURN_PARAM = "_out_param"
    const val HEADER = """//Auto generated.
#include <gmp.h>
#include <stdio.h>
"""

    const val FILE_EXT = ".rn"
}

object Operators {
    operator fun get(funcText: String) = when (funcText) {
        "+" -> "mpq_add"
        "*" -> "mpq_mul"
        "-" -> "mpq_sub"
        "/" -> "mpq_div"
        else -> throw IllegalArgumentException("No such operation")
    }
}

object Builtins {
    operator fun contains(f: String) = f in setOf("<<", ">>")

    operator fun get(f: String, arg: String, indent: Int = 0) = when (f) {
        "<<" -> "mpq_out_str(stdout, 10, $arg);"
        ">>" -> "mpq_inp_str($arg, stdin, 10);\n${" ".repeat(4 * indent)}mpq_canonicalize($arg);"
        else -> throw IllegalArgumentException("No such built-in function")
    }
}
