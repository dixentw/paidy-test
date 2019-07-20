package menu

import org.scalatest._

class MenuSpec extends FlatSpec {
  "The Menu object" should "contains 50 entries" in {
    assert(Menu.getItems().size != 0)
    assert(Menu.getItems().size == 50)
  }
  "every value (prepare time)" should "betwen 5 to 15 min" in {
    for ((k,v) <- Menu.getItems()) {
      assert(v >= 5)
      assert(v <= 15)
    }
  }
}
