// "Propagate 'MyExperimentalAPI' opt-in requirement to containing class 'Bar'" "true"
// IGNORE_FIR
// COMPILER_ARGUMENTS: -opt-in=kotlin.RequiresOptIn
// WITH_STDLIB

@RequiresOptIn
annotation class MyExperimentalAPI

@MyExperimentalAPI
fun foo() {}

class Bar {
    fun bar() {
        foo<caret>()
    }
}
