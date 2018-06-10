import org.scalatest.FunSuite

class TypeClassesTest extends FunSuite {
  test("TypeClasses.appendItems") {
    assert(TypeClasses.appendItems("Hello ", "World") === "Hello World")
    assert(TypeClasses.appendItems(1, 2) === 3)
  }
}