// "Remove annotation" "true"
// RUNTIME_WITH_SCRIPT_RUNTIME

@RequiresOptIn
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class SomeOptInAnnotation

class Foo(<caret>@SomeOptInAnnotation val value: Int) {
}
