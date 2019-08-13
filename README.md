# kotlin-result

Error handling alternative to Exception.

## Include

### Gradle

`compile "io.github.jason5lee:kotlin-result:1.1.0"`

## Example

Read two real numbers, print the sum of the square roots of them.

```kotlin
import io.github.jason5lee.result.*
import kotlin.math.sqrt

fun readLine(): Result<String, String> =
    kotlin.io.readLine().let {
        if (it == null) {
            Err("No input available.")
        } else {
            Ok(it)
        }
    }

fun String.parseDouble(): Result<Double, String> = resultTry {
    toDouble()
}.mapErr { "\"$this\" is not a valid real number." }

fun Double.squareRoot(): Result<Double, String> =
    sqrt(this).let {
        if (it.isNaN()) {
            Err("\"$this\" is not a non-negative number.")
        } else {
            Ok(it)
        }
    }

fun readSquareRoot(prompt: String): Result<Double, String> {
    print(prompt)
    return readLine().getOr { return it }
        .parseDouble().getOr { return it }
        .squareRoot()
}

fun run(): Result<Unit, String> {
    val a = readSquareRoot("Enter the first number: ")
        .getOr { return it }
    val b = readSquareRoot("Enter the second number: ")
        .getOr { return it }
    return Ok(println("The sum of the square roots of the numbers is ${a + b}."))
}

fun main() {
    run().let {
        if (it is Err) {
            print("Error: ")
            println(it.err)
        }
    }
}
```

Note that we use `getOr { return it }` to handle the error. You can also use `andThen` and `map` methods.

```kotlin
// ...

fun readSquareRoot(prompt: String): Result<Double, String> {
    print(prompt)
    return readLine()
        .andThen { it.parseDouble() }
        .andThen { it.squareRoot() }
}

fun run(): Result<Unit, String> =
    readSquareRoot("Enter the first number: ").andThen { a ->
        readSquareRoot("Enter the second number: ").map { b ->
            println("The sum of the square roots of the numbers is ${a + b}.")
        }
    }

// ...
```

In 1.1.0, `thenMap` can also be used in sequential process.

```kotlin
//...

fun run(): Result<Unit, String> =
    readSquareRoot("Enter the first number: ")
        .thenMap({ readSquareRoot("Enter the second number: ") }) { a, b ->
            println("The sum of the square roots of the numbers is ${a + b}.")
        }

// ...
```

## Further Reading

* [Railway oriented programming](https://fsharpforfunandprofit.com/posts/recipe-part2/)
