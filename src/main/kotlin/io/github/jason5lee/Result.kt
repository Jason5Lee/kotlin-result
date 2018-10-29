package io.github.jason5lee

/**
 * An object that represents either success([Ok]) or failure([Err]).
 */
sealed class Result<out T, out E> {
    /**
     * If this is [Ok], return [Ok.value]. Otherwise, apply [onErr] to [Err.err].
     *
     * A common use is `.get { return it }` in a function. It's similar to `?` in Rust
     * that when `r` is [Ok], the expression results [Ok.value],
     * when `r` is [Err], the function will return `r`.
     */
    inline fun get(onErr: (Err<E>) -> Nothing): T = when (this) {
        is Ok -> value
        is Err -> onErr(this)
    }

    /**
     * If this is [Ok], apply [f] to [Ok.value].
     * Otherwise, return this.
     */
    inline fun <U> map(f: (T) -> U): Result<U, E> = when (this) {
        is Ok -> Ok(f(value))
        is Err -> this
    }

    /**
     * If this is [Err], apply [f] to [Err.err].
     * Otherwise, return this.
     */
    inline fun <F> mapErr(f: (E) -> F): Result<T, F> = when (this) {
        is Ok -> this
        is Err -> Err(f(err))
    }
}

data class Ok<out T>(val value: T): Result<T, Nothing>()
data class Err<out E>(val err: E): Result<Nothing, E>()

/**
 * If this is [Ok], return [Ok.value].
 * Otherwise, throw [Err.err].
 */
fun <T, E: Exception> Result<T, E>.get(): T = get { throw it.err; }

/**
 * For a [Sequence] of [Result], if each element of the sequence is [Ok],
 * collect these [Ok.value]s into a [List].
 * Otherwise, returns the first [Err] appears in [Sequence].
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
 * If this is [Ok], apply [f] to [Ok.value].
 * Otherwise, return this.
 */
inline fun <T, E, R> Result<T, E>.andThen(f: (T) -> Result<R, E>): Result<R, E> = when (this) {
    is Ok -> f(value)
    is Err -> this
}

/**
 * If this is [Err], apply [f] to [Err.err].
 * Otherwise, return this.
 */
inline fun <T, E, F> Result<T, E>.orElse(f: (E) -> Result<T, F>): Result<T, F> = when (this) {
    is Ok -> this
    is Err -> f(err)
}

/**
 * Calls [block], returns its value as [Ok].
 *
 * If any subclass of [Exception] except [InterruptedException] is throw,
 * it will be caught and returned as [Err].
 *
 * [InterruptedException] is treated specially.
 * When such an exception is caught,
 * it will be passed to [onInterrupted] and throw.
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