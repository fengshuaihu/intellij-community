// "Opt in for 'A' in containing file 'newFileAnnotationWithPackage.kts'" "true"
// ACTION: Add '-opt-in=p.NewFileAnnotationWithPackage.A' to module light_idea_test_case compiler arguments
// ACTION: Opt in for 'A' in containing file 'newFileAnnotationWithPackage.kts'
// ACTION: Opt in for 'A' on 'g'
// ACTION: Opt in for 'A' on statement
// ACTION: Propagate 'A' opt-in requirement to 'g'
// RUNTIME_WITH_SCRIPT_RUNTIME
package p

@RequiresOptIn
annotation class A

@A
fun f() {}

fun g() {
    <caret>f()
}
