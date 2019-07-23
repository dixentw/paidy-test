import orderstore._
import scala.util.Random
import menu._

object Main extends App {

  private val r = new scala.util.Random
  val menuList = InMemoryOrderStoreImpl.queryMenu()
  var item = menuList(r.nextInt(menuList.length))
  var table = 1 + r.nextInt(101)

  for (a <- 0 to 15) {
    new Thread(new HeavilyConflict (table, item, InMemoryOrderStoreImpl)).start
  }

  item = menuList(r.nextInt(menuList.length))
  table = 1 + r.nextInt(101)
  for (a <- 0 to 15) {
    new Thread(new HeavilyConflictMultiItems  (table, item, InMemoryOrderStoreImpl)).start
  }

  println("finish")

}
