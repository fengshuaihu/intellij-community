// "Access static 'R.rr' via class 'R' reference" "true-preview"

class AClass
{
    public static class R {
        static int rr = 0;
    }
    public R getR() {
        return null;
    }
}
class ss {
    void f(AClass d){
        int i = <caret>d.getR().rr;
    }

}
