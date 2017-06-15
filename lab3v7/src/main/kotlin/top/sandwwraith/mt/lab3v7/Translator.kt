package top.sandwwraith.mt.lab3v7

import org.antlr.v4.runtime.tree.TerminalNode

/**
 * @author Leonid Startsev
 *		  sandwwraith@gmail.com
 * 		  ITMO University, 2017
 **/

class RatNumTranslator : RatNumsBaseVisitor<String>() {

    private fun StringBuilder.al(a: String) {
        for (i in 0 until (4 * indent)) append(" ")
        append(a)
        append(System.lineSeparator())
    }

    private var indent = 0

    private val contextStack: MutableList<Context> = ArrayList()

    private fun newScope(block: () -> Unit) {
        indent++
        val c = if (contextStack.isNotEmpty()) contextStack.last().counter else 0
        contextStack.add(Context(c))
        block()
        if (contextStack.last().allocated.isNotEmpty()) println("WARNING: not all variables have been deallocated")
        contextStack.removeAt(contextStack.size - 1)
        indent--
    }

    private val curContext
        get() = contextStack.last()


    override fun aggregateResult(aggregate: String?, nextResult: String?) = (aggregate ?: "") + (nextResult ?: "")

    override fun visitProgram(ctx: RatNumsParser.ProgramContext) = buildString {
        al(Constants.HEADER)
        ctx.func().forEach {
            append(visitFunc(it))
            al("")
        }
        ctx.main()?.let {
            append(visitMain(it))
        }
    }

    override fun visitFunc(ctx: RatNumsParser.FuncContext) = buildString {
        val name = ctx.ID(0).toString()
        val args = ctx.ID().subList(1, ctx.ID().size).map { it.toString() }
        val argl = (args.asSequence() + Constants.RETURN_PARAM).map { "${Constants.MPQ_TYPE} $it" }
        al("void $name(${argl.joinToString()}) {")
        newScope {
            ctx.lines()?.let { append(visitLines(it)) }
            append(visitRet(ctx.ret()))
            append(deallocateVars(curContext))
        }
        al("}")
    }

    override fun visitMain(ctx: RatNumsParser.MainContext) = buildString {
        al("int main() {")
        newScope {
            append(visitLines(ctx.lines()))
            append(deallocateVars(curContext))
            al("return 0;")
        }
        al("}")
    }

    private fun StringBuilder.checkVar(v: String) {
        if (contextStack.asReversed().all { v !in it.allocated })
            append(introduceVariable(v))
    }

    private fun deallocateVars(context: Context) = buildString {
        if (context.allocated.size != 0) {
            al("mpq_clears(${context.allocated.joinToString()}, 0);")
            context.allocated.clear()
        }
    }

    override fun visitRet(ctx: RatNumsParser.RetContext) = buildString {
        val txt = visitArithm(ctx.arithm())
        append(txt)
        val resultVar = curContext.popStack()
        al("mpq_set(${Constants.RETURN_PARAM}, $resultVar);")
    }

    override fun visitExt_call(ctx: RatNumsParser.Ext_callContext) = buildString {
        al(ctx.text + ";")
    }

    override fun visitCall(ctx: RatNumsParser.CallContext) = buildString {
        val funName = ctx.ID(0).toString()
        val args = ctx.ID().subList(1, ctx.ID().size - 1).map { it.toString() }
        val outParam = ctx.ID().last().toString()
        checkVar(outParam)
        al("$funName(${if (args.isNotEmpty()) args.joinToString(postfix = ", ") else ""}$outParam);")
    }

    override fun visitIo(ctx: RatNumsParser.IoContext) = buildString {
        val dir = ctx.getChild(0).text
        ctx.ID().asSequence()
                .map { it.toString() }
                .onEach { checkVar(it) }
                .forEach { al(Builtins[dir, it, indent]) }
    }

    override fun visitDef(ctx: RatNumsParser.DefContext) = introduceVariable(ctx.ID().toString())

    override fun visitAssign(ctx: RatNumsParser.AssignContext) = buildString {
        val varname = ctx.ID().toString()
        checkVar(varname)
        append(visitArithm(ctx.arithm()))
        val resultVar = curContext.popStack()
        al("mpq_set($varname, $resultVar);")
        curContext.releaseTmpVar(resultVar)
    }

    override fun visitArithm(ctx: RatNumsParser.ArithmContext): String {
        if (ctx.childCount == 1) return visitFst(ctx.fst())
        return performBinaryOp(visitArithm(ctx.arithm()), visitFst(ctx.fst()), ctx.getChild(1).text)
    }

    override fun visitFst(ctx: RatNumsParser.FstContext): String {
        if (ctx.childCount == 1) return visitScnd(ctx.scnd())
        return performBinaryOp(visitFst(ctx.fst()), visitScnd(ctx.scnd()), ctx.getChild(1).text)
    }

    private fun performBinaryOp(txt1: String, txt2: String, op: String) = buildString {
        append(txt1)
        append(txt2)
        val (resvar, new) = curContext.getTmpVar()
        if (new) append(introduceVariable(resvar))
        val funtxt = Operators[op]
        val op2 = curContext.popStack()
        val op1 = curContext.popStack()
        al("$funtxt($resvar, $op1, $op2);")
        with(curContext) {
            pushStack(resvar)
            releaseTmpVar(op1)
            releaseTmpVar(op2)
        }
    }

    override fun visitScnd(ctx: RatNumsParser.ScndContext): String {
        if (ctx.childCount == 1) return visitPrimary(ctx.primary())
        return buildString {
            append(visitScnd(ctx.scnd()))
            val (resvar, new) = curContext.getTmpVar()
            if (new) append(introduceVariable(resvar))
            val op = curContext.popStack()
            al("mpq_neg($resvar, $op);")
            curContext.pushStack(resvar)
            curContext.releaseTmpVar(op)
        }
    }

    override fun visitPrimary(ctx: RatNumsParser.PrimaryContext): String {
        if (ctx.childCount > 1) {
            return visitArithm(ctx.arithm())
        }
        val node = ctx.getChild(0) as TerminalNode
        if (node.symbol.type == RatNumsParser.ID) {
            curContext.pushStack(node.text)
            return ""
        } else {
            return buildString {
                val (varname, new) = curContext.getTmpVar()
                if (new)
                    append(introduceVariable(varname))
                al("mpq_set_ui($varname, ${node.text}, 1);")
                curContext.pushStack(varname)
            }
        }
    }

    private fun introduceVariable(varname: String) = buildString {
        curContext.allocated.add(varname)
        al("${Constants.MPQ_TYPE} $varname;")
        al("mpq_init($varname);")
    }

    override fun visitIfCond(ctx: RatNumsParser.IfCondContext) = buildString {
        al("if (${visitCond(ctx.cond())}) {")
        newScope {
            append(visitLines(ctx.lines(0)))
            append(deallocateVars(curContext))
        }
        al("}")
        if (ctx.lines().size > 1) {
            al("else {")
            newScope {
                append(visitLines(ctx.lines(1)))
                append(deallocateVars(curContext))
            }
            al("}")
        }
    }

    override fun visitWhileCond(ctx: RatNumsParser.WhileCondContext) = buildString {
        al("while (${visitCond(ctx.cond())}) {")
        newScope {
            append(visitLines(ctx.lines()))
            append(deallocateVars(curContext))
        }
        al("}")
    }

    override fun visitCond(ctx: RatNumsParser.CondContext): String {
        if (ctx.childCount == 1) return visitAndd(ctx.andd())
        else return "(${visitCond(ctx.cond())} || ${visitAndd(ctx.andd())})"
    }

    override fun visitAndd(ctx: RatNumsParser.AnddContext): String {
        if (ctx.childCount == 1) return visitComps(ctx.comps())
        else return "(${visitAndd(ctx.andd())} && ${visitComps(ctx.comps())})"
    }

    override fun visitCompEq(ctx: RatNumsParser.CompEqContext): String {
        return "(mpq_equal(${ctx.ID(0)}, ${ctx.ID(1)}) != 0)"
    }

    override fun visitCompLt(ctx: RatNumsParser.CompLtContext): String {
        return "(mpq_cmp(${ctx.ID(0)}, ${ctx.ID(1)}) < 0)"
    }

    override fun visitCompLte(ctx: RatNumsParser.CompLteContext): String {
        return "(mpq_cmp(${ctx.ID(0)}, ${ctx.ID(1)}) <= 0)"
    }

    override fun visitCompGt(ctx: RatNumsParser.CompGtContext): String {
        return "(mpq_cmp(${ctx.ID(0)}, ${ctx.ID(1)}) > 0)"
    }

    override fun visitCompGte(ctx: RatNumsParser.CompGteContext): String {
        return "(mpq_cmp(${ctx.ID(0)}, ${ctx.ID(1)}) >= 0)"
    }

    override fun visitCompNe(ctx: RatNumsParser.CompNeContext): String {
        return "(mpq_equal(${ctx.ID(0)}, ${ctx.ID(1)}) == 0)"
    }

    override fun visitCompBrackets(ctx: RatNumsParser.CompBracketsContext): String {
        return "(${visitCond(ctx.cond())})"
    }
}

class Context(initCounter: Int = 0) {
    val allocated: MutableSet<String> = HashSet()

    private val tmpVars: MutableSet<String> = HashSet()
    private val freeTmps: MutableSet<String> = LinkedHashSet()
    var counter = initCounter
        private set

    private val exprStack: MutableList<String> = ArrayList()

    fun createTmpVar(): String {
        val varname = "_tmp_var${counter++}"
        allocated.add(varname)
        tmpVars.add(varname)
        return varname
    }

    fun getTmpVar(): Pair<String, Boolean> {
        return if (freeTmps.isNotEmpty()) {
            val r = freeTmps.first()
            freeTmps.remove(r)
            r to false
        } else createTmpVar() to true
    }

    fun releaseTmpVar(v: String) = if (v in tmpVars) freeTmps.add(v) else false

    fun pushStack(v: String) = exprStack.add(v)

    fun popStack(): String {
        val r = exprStack.last()
        exprStack.removeAt(exprStack.size - 1)
        return r
    }
}
