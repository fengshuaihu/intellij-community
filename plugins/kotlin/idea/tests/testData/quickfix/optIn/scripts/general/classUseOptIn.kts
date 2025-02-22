// "Opt in for 'MyExperimentalAPI' on containing class 'Bar'" "true"
// PRIORITY: HIGH
// ACTION: Add '-opt-in=a.b.ClassUseOptIn.MyExperimentalAPI' to module light_idea_test_case compiler arguments
// ACTION: Opt in for 'MyExperimentalAPI' in containing file 'classUseOptIn.kts'
// ACTION: Opt in for 'MyExperimentalAPI' on 'bar'
// ACTION: Opt in for 'MyExperimentalAPI' on containing class 'Bar'
// ACTION: Opt in for 'MyExperimentalAPI' on statement
// ACTION: Propagate 'MyExperimentalAPI' opt-in requirement to 'bar'
// RUNTIME_WITH_SCRIPT_RUNTIME

package a.b

@RequiresOptIn
@Target(AnnotationTarget.FUNCTION)
annotation class MyExperimentalAPI

@MyExperimentalAPI
fun foo() {}

class Bar {
    fun bar() {
        foo<caret>()
    }
}
