def a():
    yield 1
    return 'a'


def f(x: int) -> int:
    return x


def test():
    return f(<warning descr="Expected type 'int', got 'LiteralString' instead">(yield from a())</warning>)