package top.sandwwraith.mt.lab3v7

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.util.*

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

class ErrorCollector : BaseErrorListener() {
    private val errors : MutableList<String> = ArrayList()

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException?) {
        errors.add("Line $line:$charPositionInLine, $msg")
    }

    internal fun hasErrors(): Boolean {
        return errors.size != 0
    }

    fun getErrors(): List<String> = errors

    internal fun addError(errorMsg: String) {
        errors.add(errorMsg)
    }
}