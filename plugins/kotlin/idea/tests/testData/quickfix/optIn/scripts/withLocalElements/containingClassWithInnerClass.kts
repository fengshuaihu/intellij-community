// "Opt in for 'Library' on statement" "true"
// ACTION: Add '-opt-in=ContainingClassWithInnerClass.Library' to module light_idea_test_case compiler arguments
// ACTION: Do not show return expression hints
// ACTION: Introduce local variable
// ACTION: Opt in for 'Library' in containing file 'containingClassWithInnerClass.kts'
// ACTION: Opt in for 'Library' on 't5'
// ACTION: Opt in for 'Library' on containing class 'T4'
// ACTION: Opt in for 'Library' on containing class 'T5'
// ACTION: Opt in for 'Library' on statement
// RUNTIME_WITH_SCRIPT_RUNTIME
@RequiresOptIn
annotation class Library()

@Library
class MockLibrary


@Library
val foo: MockLibrary = MockLibrary();

{
    class T4() {
        inner class T5() {
            fun t5() {
                foo<caret>
            }
        }
    }
}