package zygf.tests

class TestSuite extends org.scalatest.funsuite.AnyFunSuite
{
  def testNumEval(name: String, count: Int)(body: (Any => Any) => Any) = test(name) {
    var i = 0
    def f(any: Any) = { i += 1; any }
    body(f _)
    assert(i == count, s"Evaluated ${i} times instead of ${count}")
  }
}
