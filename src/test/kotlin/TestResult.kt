import io.github.jason5lee.result.*
import org.junit.Assert.*
import org.junit.Test

class TestResult {
    data class TestException(val flag: Boolean = true) : Exception()

    @Test
    fun testIs() {
        assert(Ok(1).isOk())
        assertFalse(Ok(1).isErr())
        assert(Err(1).isErr())
        assertFalse(Err(1).isOk())
    }

    @Test
    fun testMap() {
        assertEquals(Ok(10), Ok(2).map { it * 5 })
        val err: Result<Int, TestException> = Err(TestException())
        assertEquals(Err(TestException()), err.map { it * 5 })
    }

    @Test
    fun testMapErr() {
        assertEquals(Err(TestException()),
                Err(TestException(false)).mapErr { TestException(!it.flag) })
    }

    @Test
    fun testGetOr() {
        fun okFun(): Result<Int, TestException> = Ok(3)
        fun errFun(): Result<Int, TestException> = Err(TestException())

        fun combineFun(): Result<Int, TestException> {
            errFun().getOr { return it }
            okFun().getOr { return it } // Return
            return Err(TestException(false))
        }

        assertEquals(Err(TestException()), combineFun())
    }

    @Test
    fun testUnwrapOk() {
        assertEquals("", Ok("").unwrap())
    }

    @Test(expected = TestException::class)
    fun testUnwrapErr() {
        val r: Result<Int, TestException> = Err(TestException())
        r.unwrap()
    }

    @Test
    fun testCollect() {
        assertEquals(sequenceOf(Ok(1), Ok(2), Ok(3)).collect(),
                Ok(listOf(1, 2, 3)))
        assertEquals(sequenceOf(Ok(1), Err(TestException()), Err(TestException(false)), Ok(3)).collect(),
                Err(TestException()))
    }

    @Test
    fun testAndThen() {
        assertEquals(Ok("1"), Ok(1).andThen { Ok(it.toString()) })
        assertEquals(Err(TestException()),
                Ok(false).andThen { Err(TestException(!it)) })
        assertEquals(Err(TestException()),
                Err(TestException()).andThen<Nothing, TestException, Nothing> {
                    return assertTrue(false)
                })
    }

    @Test
    fun testOrElse() {
        assertEquals(Ok(1), Ok(1).orElse<Int, Nothing, Nothing> {
            return assertTrue(false)
        })
        assertEquals(Ok(true),
                Err(TestException()).orElse { Ok(it.flag) })
        assertEquals(Err(TestException()),
                Err(TestException(false)).orElse { Err(TestException()) })
    }

    @Test
    fun testResultTry() {
        assertEquals(Ok(11), resultTry { 11 })
        assertEquals(Err(TestException()),
                resultTry { throw TestException() })
    }

    @Test(expected = StackOverflowError::class)
    fun testResultTryOnError() {
        fun stackOverFlow(a: Int): Int =
                if (a == 0) {
                    a
                } else {
                    stackOverFlow(a - 1) * a // Avoid being optimized to a loop.
                }

        resultTry { stackOverFlow(-1) }
    }

    @Test
    fun testInterruptionInResultTry() {
        var onInterruptedFlag = false
        var catchFlag = false
        val thread = Thread {
            try {
                resultTry(onInterrupted = { onInterruptedFlag = true }) {
                    while (true) {
                        Thread.sleep(10)
                    }
                }
            } catch (_: InterruptedException) {
                catchFlag = true
            }
        }
        thread.start()
        assertFalse(onInterruptedFlag)
        thread.interrupt()
        thread.join()
        assertTrue(onInterruptedFlag)
        assertTrue(catchFlag)
    }

    @Test
    fun testThenMap() {
        assertEquals(Ok(2 + 2*3), Ok(2).thenMap({ Ok(it * 3) }, Int::plus))
        assertEquals(Err(TestException()), Ok(2).thenMap({ Err(TestException()) }) { _, _ -> Unit })
        assertEquals(Err(TestException(false)), Err(TestException(false)).thenMap({ Ok(2) }) { _, _ -> Unit })
    }
}