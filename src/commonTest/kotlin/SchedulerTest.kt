import com.blackstone.library.OnAppBoot
import com.blackstone.library.ScheduledTask
import com.blackstone.library.register
import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

internal expect fun <T> runTest(block: suspend () -> T): T

@OptIn(ExperimentalTime::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
class SchedulerTest {

    @Test
    fun `it should execute a simple on boot operation`() = runTest {
        // given:
        var varToUpdate = ""

        // when:
        register {
            coroutinesScope = GlobalScope
            task = {
                varToUpdate = "something"
            }
            use(OnAppBoot) {
                runWhen = {
                    true
                }
            }
        }
        // this is a hack!!!! not sure why run blocking is running out of order. :/
        delay(100)
        // then:
        assertEquals("something", varToUpdate)
    }


    @Test
    fun `it should not execute an on boot operation when the predicate is false`() = runTest {
        var varToUpdate = ""

        // when:
        register {
            coroutinesScope = GlobalScope
            task = {
                varToUpdate = "something"
            }
            use(OnAppBoot) {
                runWhen = {
                    false
                }
            }
        }
        // this is a hack!!!! not sure why run blocking is running out of order. :/
        delay(100)
        // then:
        assertEquals("", varToUpdate)
    }


    @Test
    fun `it should execute an operation multiple times`() = runTest {
        val addToList = mutableListOf<String>()
        var count = 0
        // when:
        register {
            coroutinesScope = GlobalScope
            task = {
                addToList.add("$count")
                count ++
            }
            use(ScheduledTask) {
                this.startTime = Duration.ZERO // no delay
                this.runEvery = Duration.milliseconds(100)
                this.runWhen = {true} // run always
            }
        }
        // this is a hack!!!! not sure why run blocking is running out of order. :/
        delay(450)
        // then:
        assertEquals(5, count)
        assertEquals(listOf("0", "1", "2", "3", "4"), addToList.toList())
    }


    @Test
    fun `it should skip an item when running and predicate turns to false`() = runTest {
        val addToList = mutableListOf<String>()
        var count = 0
        // when:
        register {
            coroutinesScope = GlobalScope
            task = {
                addToList.add("$count")
                count++
            }
            use(ScheduledTask) {
                this.startTime = Duration.ZERO // no delay
                this.runEvery = Duration.milliseconds(100)
                this.runWhen = {
                val ret = count != 1
                    if(!ret) count ++ // so we don't stop our execution
                    ret
                } // run always
            }
        }
        // this is a hack!!!! not sure why run blocking is running out of order. :/
        delay(450)
        // then:
        assertEquals(5, count)
        assertEquals(listOf("0",  "2", "3", "4"), addToList.toList())
    }

}