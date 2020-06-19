package `in`.kyle.mcspring.commands.dsl.parsers.numbers

import `in`.kyle.mcspring.commands.dsl.CommandTestSupport.runCommand
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.assertj.core.api.Assertions.fail

class TestIntParser : FreeSpec({

    "should parse int" - {
        "should parse simple int" - {
            runCommand("123") {
                val arg0 = intArg {
                    invalid { fail("should not run") }
                    missing { fail("should not run") }
                }
                arg0 shouldBe 123
            }
        }

        "should parse any int" - {
            checkAll<Int> {
                runCommand("$it") {
                    val testArg = intArg {
                        invalid { fail("should not run") }
                        missing { fail("should not run") }
                    }
                    testArg shouldBe it
                }
            }
        }
    }
})
