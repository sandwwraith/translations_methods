package top.sandwwraith.mt.lab2v6

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec

/**
 * @author Leonid Startsev
 * *         sandwwraith@gmail.com
 * *         ITMO University, 2017
 */
class AppTest : ShouldSpec() {
    init {
        should("Return greeting string") {
            helloWorldGenerator() shouldBe "Hello, world!"
        }
    }
}