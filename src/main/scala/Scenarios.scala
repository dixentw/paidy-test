import orderstore.OrderStore
import scala.util.control._
import scala.util.Random
import scala.math.max

// every threads order the same item for the same table.
class HeavilyConflict (
  private val table: Int,
  private val item: Int,
  private val service: OrderStore
) extends Runnable {

  def run (): Unit = {
    val tn = Thread.currentThread.getId()
    val loop = new Breaks
    var version = 0
    var originCount = 0

    loop.breakable {
      while (true) {// retry to success
        val orders = service.queryOrderByTable(table)
        if (orders.length != 0) {
          version = orders(0).version
          originCount = orders(0).itemCount
        }
        println(s"read ${orders}, $tn")
        val res = service.addOrders((table, item, version)).flatten
        if (res.length == 1) {
          println(s"mod ${res}, $tn")
          assert(res(0).itemCount == originCount + 1, s"add assert $tn")
          loop.break
        }
      }
    }
    loop.breakable {
      while (true) {// retry to success
        val orders = service.queryOrderByTable(table)
        if (orders.length != 0) {
          version = orders(0).version
          originCount = orders(0).itemCount
        }
        println(s"readr ${orders}, $tn")
        try {
          val order = service.removeOrder(table, item, version)
          println(s"modr ${order}, $tn")
          assert(order.itemCount == originCount - 1, s" remove error $tn")
          loop.break
        } catch {
          case m: orderstore.ModifyException =>
          case e: Exception => {
            println(e)
            loop.break
          }
        }
      }
    }
  }
}

// every threads order the same amaount of items for the same table.
class HeavilyConflictMultiItems (
  private val table: Int,
  private val item: Int,
  private val service: OrderStore
) extends Runnable {

  def run (): Unit = {
    val tn = Thread.currentThread.getId()
    val loop = new Breaks

    var incrCount = 2
    var version = 0
    var originCount = 0
    loop.breakable {
      while (true) {
        if (incrCount == 0) loop.break
        val orders = service.queryOrderByTable(table)
        if (orders.length != 0) {
          version = orders(0).version
          originCount = orders(0).itemCount
        }
        val input = (0 to incrCount).map((i) => (table, item, version+i))
        println(s">>> read ${orders}, ${tn}")
        val res = service.addOrders(input:_*)
        var success:Int = 0
        var fail:Int = 0
        var maxCount: Int = originCount
        for (orderOption <- res) {
          orderOption match {
            case Some(order) => {
              success += 1
              println(order)
              maxCount = max(maxCount, order.itemCount)
            }
            case None => fail += 1
          }
        }
        assert(maxCount == originCount + success,
          "%s, %d, %d, %d".format(tn, maxCount, originCount, success))
        if (fail == 0) loop.break
        incrCount -= success
      }
    }
  }
}
