import io.github.jason5lee.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test


class TestResult {
    data class TestException(val flag: Boolean = true): Exception()

    @Test
    fun testGet() {
        assertEquals("", Ok("").get())

        fun okFun(): Result<Int, TestException> = Ok(3)
        fun errFun(): Result<Int, TestException> = Err(TestException())

        fun combineFun(): Result<Int, TestException> {
            errFun().get { return it }
            okFun().get { return it }
            return Err(TestException(false))
        }

        assertEquals(Err(TestException()), combineFun())
    }

    @Test(expected = TestException::class)
    fun testGetDefault() {
        Err(TestException()).get()
    }

    @Test
    fun testCollect() {
        assertEquals(sequenceOf(Ok(1), Ok(2), Ok(3)).collect(),
                Ok(listOf(1, 2, 3)))
        assertEquals(sequenceOf(Ok(1), Err(TestException()), Err(TestException(false)), Ok(3)).collect(),
                Err(TestException()))
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
}