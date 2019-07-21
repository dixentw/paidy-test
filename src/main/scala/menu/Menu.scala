package menu

import scala.util.Random

//Menu generate items
object Menu {

  private val prepareInterval = Array(5, 15)
  private val r = new scala.util.Random

  private val items = (1 to 50).foldLeft(Map.empty[Int,Int]) {
    (m, i) => m + (i -> (5+r.nextInt(prepareInterval(1)-prepareInterval(0)+1)))
  }

  def getItems(): Seq[(Int, Int)] = items.toSeq

  def prepareTime(itemId: Int): Int = items(itemId)

  def isExist(item: Int): Boolean = {
    items.contains(item)
  }
}
