package io.github.jason5lee.result

/**
 * A discriminated union of either an [Ok] value or an [Err]or.
 */
sealed class Result<out T, out E> {
    /**
     * Returns `true` if this is [Ok].
     */
    fun isOk(): Boolean = this is Ok

    /**
     * Returns `true` if this is [Err].
     */
    fun isErr(): Boolean = this is Err

    /**
     * If this is [Ok], returns [Ok] of applying [f] to the [Ok.value],
     * otherwise returns this.
     */
    inline fun <U> map(f: (T) -> U): Result<U, E> = when (this) {
        is Ok -> Ok(f(value))
        is Err -> this
    }

    /**
     * If this is [Err], returns an [Err] of applying [f] to the [Err.err],
     * otherwise returns this.
     */
    inline fun <F> mapErr(f: (E) -> F): Result<T, F> = when (this) {
        is Ok -> this
        is Err -> Err(f(err))
    }
}

data class Ok<out T>(val value: T): Result<T, Nothing>()
data class Err<out E>(val err: E): Result<Nothing, E>()

/**
 * If this is [Ok], returns [Ok.value],
 * otherwise returns applying [onErr] to this.
 *
 * Note that `r.getOr { return it }` has a similar effect as `r?` in Rust,
 * which is, if `r` is [Ok], the expression results [Ok.value],
 * otherwise `r` is returned.
 */
inline fun <T, E> Result<T, E>.getOr(onErr: (Err<E>) -> T): T = when (this) {
    is Ok -> value
    is Err -> onErr(this)
}

/**
 * If this is [Ok], returns [Ok.value],
 * otherwise throws [Err.err].
 */
fun <T, E: Throwable> Result<T, E>.unwrap(): T = getOr { throw it.err; }

/**
 * For a [Sequence] of [Result], if each element of the sequence is [Ok],
 * collect these [Ok.value]s into a [List],
 * otherwise returns the first [Err] appears in [Sequence].
 */
fun <T, E> Sequence<Result<T, E>>.collect(): Result<List<T>, E> {
    val resultList = mutableListOf<T>()
    for (ele in this) {
        when (ele) {
            is Ok -> resultList.add(ele.value)
            is Err -> return ele
        }
    }
    return Ok(resultList.toList())
}

/**
 * If this is [Ok], returns applying [f] to [Ok.value],
 * otherwise returns this.
 */
inline fun <T, E, R> Result<T, E>.andThen(f: (T) -> Result<R, E>): Result<R, E> = when (this) {
    is Ok -> f(value)
    is Err -> this
}

/**
 * If this is [Err], returns applying [f] to [Err.err],
 * otherwise returns this.
 */
inline fun <T, E, F> Result<T, E>.orElse(f: (E) -> Result<T, F>): Result<T, F> = when (this) {
    is Ok -> this
    is Err -> f(err)
}

/**
 * Calls [block], returns an [Ok] of its result.
 *
 * If any subclass of [Exception] except [InterruptedException] is throw,
 * it will be caught and returned in an [Err].
 *
 * [InterruptedException] is treated specially.
 * When such an exception is caught,
 * it will be passed to [onInterrupted] and re-throw.
 */
inline fun <T> resultTry(
        onInterrupted: (InterruptedException) -> Unit = {},
        block: () -> T): Result<T, Exception> = try {
    Ok(block())
} catch (e: InterruptedException) {
    onInterrupted(e)
    throw e
} catch (e: Exception) {
    Err(e)
}

/**
 * Equivalent to `andThen { x -> f(x).map { y -> g(x, y) } }`,
 * useful for sequential process.
 */
inline fun <T1, T2, T3, E> Result<T1, E>.thenMap(f: (T1) -> Result<T2, E>, g: (T1, T2) -> T3): Result<T3, E> =
        andThen { x -> f(x).map { y -> g(x, y) } }
