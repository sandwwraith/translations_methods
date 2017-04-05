package top.sandwwraith.mt.lab2v6

import java.io.Writer

class TreePrinter(val tree: TreeNode, var name: String = "") {

    private lateinit var writer: Writer

    fun printTo(output: Writer) {
        this.writer = output
        writer.write("digraph $name {")
        writer.write(System.lineSeparator())
        printNodeNamesRec(tree)
        printNodeLinksRec(tree)
        writer.write("}")
    }

    private fun TreeNode.uniqueId() = "node" + System.identityHashCode(this)

    private fun printNodeNamesRec(cur: TreeNode) {
        printNodeName(cur)
        cur.children.forEach { printNodeNamesRec(it) }
    }

    private fun printNodeLinksRec(cur: TreeNode) {
        printNodeLinks(cur)
        cur.children.forEach { printNodeLinksRec(it) }
    }

    private fun printNodeName(cur: TreeNode) {
        val attrs = mutableMapOf("label" to "\"${cur.name}\"")
        if (cur.term) {
            attrs["color"] = "red"
            attrs["label"] = "< <B>${attrs["label"]!!.replace("\"", "")}</B> >"
        } else if (cur.children.isEmpty()) {
            attrs["style"] = "filled"
            attrs["fillcolor"] = "lightgray"
        }
        val attrString = attrs.map { (k, v) -> "$k=$v" }.joinToString()
        writer.write("${cur.uniqueId()}[$attrString]")
        writer.write(System.lineSeparator())
    }

    private fun printNodeLinks(cur: TreeNode) {
        if (cur.children.isEmpty()) return
        val l = cur.children.map { it.uniqueId() }.joinToString(separator = " ")
        writer.write("${cur.uniqueId()} -> {$l}")
        writer.write(System.lineSeparator())
    }
}