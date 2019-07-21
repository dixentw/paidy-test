package orderstore

/*
 * The interface of OrderStore, it defines CRUD APIs for table orders
 */

trait OrderStore {
    // return valid menu ids to client
    def queryMenu(): Seq[Int]
    // return orders for given table
    def queryOrderByTable(tableId: Int): Seq[Order]
    // add an order to given table
    def addOrders(orders: (Int, Int, Int)*): Seq[Option[Order]]
    // remove an order from a given table
    @throws(classOf[ModifyException])
    @throws(classOf[NoSuchOrderException])
    def removeOrder(tableId: Int, itemId: Int, version: Int): Order
}

// Order: Result value object for add/remove/query
class Order (
  private var _table:Int=1,
  private var _item:Int=1,
  private var _itemCount:Int=0,
  private var _prepareTime:Int=0,
  private var _version:Int=1) {
  //Getter
  def table = _table
  def item = _item
  def itemCount = _itemCount
  def prepareTime = _prepareTime
  def version = _version

  override def toString: String =
    "[table:%d, item:%d, count:%d, time:%d, ver:%d]".format(_table, _item, _itemCount, _prepareTime, _version)
}

// Lock Exception, let client decide to retry
final case class ModifyException(private val message: String = "",
                           private val cause: Throwable = None.orNull)
                      extends Exception(message, cause)
// Lock Exception, let client decide to retry
final case class NoSuchOrderException(private val message: String = "",
                           private val cause: Throwable = None.orNull)
                      extends Exception(message, cause)
