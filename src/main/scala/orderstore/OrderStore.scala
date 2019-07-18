package orderstore

/*
 * The interface of OrderStore, it defines CRUD APIs for table orders
 */

trait OrderStore {
    // read orders for given table
    def queryOrderByTable(table: Int): List[(Int, Int, Int)]
    // add an order to given table
    def AddOrder(table: Int, item: Int*): Boolean
    // remove an order from a given table
    def RemoveOrder(table: Int, item: Int): Boolean
}
