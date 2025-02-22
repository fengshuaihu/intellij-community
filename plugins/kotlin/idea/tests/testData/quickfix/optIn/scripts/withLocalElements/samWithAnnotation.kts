// "Opt in for 'MyOptIn' on 'SamI'" "true"
// RUNTIME_WITH_SCRIPT_RUNTIME
// ACTION: Add '-opt-in=SamWithAnnotation.MyOptIn' to module light_idea_test_case compiler arguments
// ACTION: Do not show implicit receiver and parameter hints
// ACTION: Opt in for 'MyOptIn' in containing file 'samWithAnnotation.kts'
// ACTION: Opt in for 'MyOptIn' on 'SamI'
// ACTION: Opt in for 'MyOptIn' on 'SamI2'
// ACTION: Opt in for 'MyOptIn' on statement

@RequiresOptIn
annotation class MyOptIn

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class Bar

@MyOptIn
fun foo() {
}

fun interface SamI {
    fun run()
}

fun interface SamI2 {
    fun run()
}

SamI2 {
    @Bar
    SamI {
        foo<caret>()
    }
}
