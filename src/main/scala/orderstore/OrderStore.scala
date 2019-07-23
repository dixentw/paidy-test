package orderstore

/*
 * The interface of OrderStore, it defines CRUD APIs for table orders
 */

trait OrderStore {
    // return valid menu ids to client
    // for single source of truth purpose
    def queryMenu(): Seq[Int]

    // return orders for given table
    // [ISSUE] it will return order with item count == 0, for later update of the same item
    // let client decide the usage of this api, if client only need current orders, then remove count==0 item.
    def queryOrderByTable(tableId: Int): Seq[Order]

    // add orders to given table, return the options with the same amount of orders
    // if there was an error to add some order, the option will be None
    // FIXME: issue: the result is undeterminated.
    // For N orders with same table and item, there is no guarentee that final N valid result will be M + N (M is previous item count)
    def addOrders(orders: (Int, Int, Int)*): Seq[Option[Order]]

    // add an order to given table, it throws ModifyException if the given ver is not current version.
    // let the client decide how to try again.
    // the latest version should use queryOrderByTable to retrieve again for retry
    @throws(classOf[ModifyException])
    @throws(classOf[NoSuchOrderException])
    def incrOrder(tableId: Int, itemId: Int, ver: Int): Order
    // remove an order from a given table
    @throws(classOf[ModifyException])
    @throws(classOf[NoSuchOrderException])
    def removeOrder(tableId: Int, itemId: Int, version: Int): Order
}

// Order: Result value object for add/remove/query API
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

// Optimisic Lock Exception, let client decide to retry
final case class ModifyException(private val message: String = "",
                           private val cause: Throwable = None.orNull)
                      extends Exception(message, cause)
// Wrong Paramter Exception
final case class NoSuchOrderException(private val message: String = "",
                           private val cause: Throwable = None.orNull)
                      extends Exception(message, cause)
