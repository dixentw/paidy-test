package orderstore

import org.scalatest._
import menu.Menu

class OrderStoreImplSpec extends FlatSpec {
  // single operation
  "OrderStoreImpl" should "return menu" in {
    val menu = InMemoryOrderStoreImpl.queryMenu()
    assert(menu.length == 50)
    assert(menu.contains(1))
    assert(!menu.contains(0))
    assert(!menu.contains(51))
  }

  "OrderStoreImpl incrOrder" should "perform add order with valid item and table" in {
    val res = InMemoryOrderStoreImpl.incrOrder(1, 1, 0)
    assert(res.table == 1)
    assert(res.item == 1)
    assert(res.itemCount == 1)
    assert(res.prepareTime == Menu.prepareTime(res.item))
    assert(res.version == 1)
  }

  "OrderStoreImpl incrOrder" should "perform incr order with valid item and table" in {
    val res = InMemoryOrderStoreImpl.incrOrder(1, 1, 1)
    assert(res.table == 1)
    assert(res.item == 1)
    assert(res.itemCount == 2)
    assert(res.prepareTime == Menu.prepareTime(res.item))
    assert(res.version == 2)
  }

  "OrderStoreImpl queryOrderByTable" should "return two orders of given table" in {
    val res = InMemoryOrderStoreImpl.queryOrderByTable(1)
    assert(res.length==1)
    assert(res(0).table == 1)
    assert(res(0).item == 1)
    assert(res(0).itemCount == 2)
    assert(res(0).prepareTime == Menu.prepareTime(res(0).item))
    assert(res(0).version == 2)
  }

  "OrderStoreImpl incrOrder" should "exception if version is too old" in {
    try {
      val res = InMemoryOrderStoreImpl.incrOrder(1, 1, 1)
      fail
    } catch {
      case e: ModifyException => succeed
      case _ : Exception => fail
    }
  }

  "OrderStoreImpl incrOrder" should "exception if table is invalid" in {
    try {
      val res = InMemoryOrderStoreImpl.incrOrder(-1, 1, 1)
      fail
    } catch {
      case e: NoSuchOrderException => succeed
      case _ : Exception => fail
    }
  }

  "OrderStoreImpl incrOrder" should "exception if item is invalid" in {
    try {
      val res = InMemoryOrderStoreImpl.incrOrder(1, -1, 1)
      fail
    } catch {
      case e: NoSuchOrderException => succeed
      case _ : Exception => fail
    }
  }

  "OrderStoreImpl removeOrder" should "remove an order" in {
    val res = InMemoryOrderStoreImpl.removeOrder(1, 1, 2)
    assert(res.table == 1)
    assert(res.item == 1)
    assert(res.itemCount == 1)
    assert(res.version == 3)
  }

  "OrderStoreImpl removeOrder" should "NoSuchOrderException  if table not exist" in {
    try {
      val res = InMemoryOrderStoreImpl.removeOrder(0, 1, 1)
      fail
    } catch {
      case e: NoSuchOrderException => succeed
      case _ : Exception => fail
    }
  }

  "OrderStoreImpl removeOrder" should "NoSuchOrderException if item not exist" in {
    try {
      val res = InMemoryOrderStoreImpl.removeOrder(1, 3, 1)
      fail
    } catch {
      case e: NoSuchOrderException => succeed
      case _ : Exception => fail
    }
  }

  "OrderStoreImpl removeOrder" should "throw ModifyException if version key is old" in {
    try {
      val res = InMemoryOrderStoreImpl.removeOrder(1, 1, 1)
      fail
    } catch {
      case e: ModifyException => succeed
      case _ : Exception => fail
    }
  }

  "OrderStoreImpl queryOrderByTable" should "return 1 order of given table" in {
    val res = InMemoryOrderStoreImpl.queryOrderByTable(1)
    assert(res.length==1)
    assert(res(0).table == 1)
    assert(res(0).item == 1)
    assert(res(0).itemCount == 1)
    assert(res(0).version == 3)
  }

  "OrderStoreImpl removeOrder" should "remove last order" in {
    val res = InMemoryOrderStoreImpl.removeOrder(1, 1, 3)
    assert(res.table == 1)
    assert(res.item == 1)
    assert(res.itemCount == 0)
    assert(res.version == 4)
  }

  "OrderStoreImpl queryOrderByTable" should "return zero order of given table" in {
    val res = InMemoryOrderStoreImpl.queryOrderByTable(1)
    assert(res.length==0)
  }

  // multi operation
  "OrderStoreImpl addOrders" should "add many orders to data store" in {
    val res = InMemoryOrderStoreImpl.addOrders((1,1,0), (1,2,0), (1,3,0))
    assert(res.length==3)
    for (order <- res) {
      order match {
        case None => fail
        case _=>
      }
    }
  }

  "OrderStoreImpl addOrders" should "failed at some case that add order with old version" in {
    val res = InMemoryOrderStoreImpl.addOrders((1,1,1), (1,1,0))
    assert(res.length == 2)
    assert(res.flatten.length == 1)
  }
}
