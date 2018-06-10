import org.scalatest.FunSuite

class AdHocPolymorhismTest extends FunSuite {
  test("CubeCalculator.appendItems") {
    assert(TypeClasses.appendItems("Hello ", "World") === "Hello World")
    assert(TypeClasses.appendItems(1, 2) === 3)
  }
}