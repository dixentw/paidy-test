import orderstore._
import scala.util.Random
import menu._

object Main extends App {

  private val r = new scala.util.Random
  val menuList = InMemoryOrderStoreImpl.queryMenu()
  val item = menuList(r.nextInt(menuList.length))
  val table = 1 + r.nextInt(101)

  /*
  for (a <- 1 to 15) {
    new Thread(new HeavilyConflict (table, item, InMemoryOrderStoreImpl)).start
  }*/

  for (a <- 1 to 3) {
    new Thread(new HeavilyConflictMultiItems  (table, item, InMemoryOrderStoreImpl)).start
  }

  println("finish")

}
