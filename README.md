# kotlin-result

Error handling alternative to Exception.

## Binaries

Example for Gradle:

`compile "io.github.jason5lee:kotlin-result:1.0.1"`

## Example

Read two real numbers, print the sum of the square roots of them.

```kotlin
import io.github.jason5lee.*

object Example {
    private fun readLine(): Result<String, String> =
            kotlin.io.readLine().let {
                if (it == null) {
                    Err("No input available.")
                } else {
                    Ok(it)
                }
            }

    private fun String.parseDouble(): Result<Double, String> = resultTry {
        toDouble()
    }.mapErr { "\"$this\" is not a valid real number." }

    private fun Double.squareRoot(): Result<Double, String> =
            Math.sqrt(this).let {
                if (it.isNaN()) {
                    Err("\"$this\" is not a non-negative number.")
                } else {
                    Ok(it)
                }
            }

    private fun readParseSquareRoot(prompt: String): Result<Double, String> {
        print(prompt)
        return readLine().get { return it }
                .parseDouble().get { return it }
                .squareRoot()
    }

    private fun run(): Result<Unit, String> {
        val a = readParseSquareRoot("Enter the first number: ")
                .get { return it }
        val b = readParseSquareRoot("Enter the second number: ")
                .get { return it }
        return Ok(println("The sum of the square roots of the numbers is ${a + b}."))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        run().let {
            if (it is Err) {
                print("Error: ")
                println(it.err)
            }
        }
    }
}
```

Note that we use `.get { return it }` to handle the error. You can also use `andThen` and `map` methods.

```kotlin
// ...

object Example {
    // ...
    private fun readParseSquareRoot(prompt: String): Result<Double, String> {
        print(prompt)
        return readLine().andThen {
            it.parseDouble().andThen {
                it.squareRoot()
            }
        }
    }

    private fun run(): Result<Unit, String> =
            readParseSquareRoot("Enter the first number: ")
                    .andThen { a ->
                        readParseSquareRoot("Enter the second number: ").map { b ->
                            println("The sum of the square roots of the numbers is ${a + b}.")
                        }
                    }
    // ...
}
```

## TODO

* Better syntax for doing `.get { return it }`(maybe Coroutine).