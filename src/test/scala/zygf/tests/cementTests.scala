package zygf.tests

import zygf.cement.cement

class cementTests extends TestSuite
{
  testNumEval("def", 1) { f =>
    def cem(in: Any) = cement(f(in))
    assert(cem("foo") == "foo")
    assert(cem("bar") == "foo")
    assert(cem("baz") == "foo")
  }
  
  testNumEval("def 2x", 2) { f =>
    def cem(in: Any) = {
      cement(f(null))
      cement(f(in))
    }
    assert(cem("foo") == "foo")
    assert(cem("bar") == "foo")
    assert(cem("baz") == "foo")
  }
  
  testNumEval("2x def", 2) { f =>
    def cem1(in: Any) = cement(f(in))
    def cem2(in: Any) = cement(f(in))
    assert(cem1("foo") == "foo")
    assert(cem1("bar") == "foo")
    assert(cem1("baz") == "foo")
    assert(cem2("foo") == "foo")
    assert(cem2("bar") == "foo")
    assert(cem2("baz") == "foo")
  }
  
  testNumEval("README warning", 1) { f =>
    (1 to 3).map { i => cement(f(i)) }.toList == List(1, 1, 1)
  }
}
