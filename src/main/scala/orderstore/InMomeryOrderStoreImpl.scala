package orderstore

import menu.Menu
import scala.math.min
import scala.collection.mutable.Map
import java.util.concurrent.locks._

object InMemoryOrderStoreImpl extends OrderStore {
  // Main storage
  private[this] var mStore: Map[Int, Map[Int, Int]] = Map()
  // Versioning storage for lock
  private[this] var version: Map[String, Int] = Map()
  // the lock to prevent read while other updates
  private[this] val rwLock: ReadWriteLock = new ReentrantReadWriteLock()

  override def queryMenu(): Seq[Int] = {
    menu.Menu.getItems().map((t:(Int,Int)) => t._1)
  }

  // protected the read from other writes, can be access by many threads at the same time 
  override def queryOrderByTable(tableId: Int): Seq[Order] = {
    if (!mStore.contains(tableId)) Seq()
    else{
      rwLock.readLock().lock()
      val res = mStore(tableId).map {
        case (item, count) => new Order(tableId,
              item,
              count,
              Menu.prepareTime(item),
              version.getOrElse(versionKey(tableId, item), 1))
      }.toSeq
      rwLock.readLock().unlock()
      res
    }
  }

  // wrapper function for batch add, it is synchronized because I want it execute a batch
  // as whole for one thread. that is easier to assert the result.
  override def addOrders(orders: (Int, Int, Int)*): Seq[Option[Order]] = synchronized {
    rwLock.writeLock().lock()
    val res = orders.map {
      case (table, item, ver) => {
        try {
          Some(incrOrder(table, item, ver))
        } catch {
          case e : Exception => None
        }
      }
    }.toSeq
    rwLock.writeLock().unlock()
    res
  }

  // incrOrder : change the state of storage, only one thread can execute it at one time.
  @throws(classOf[ModifyException])
  @throws(classOf[NoSuchOrderException])
  def incrOrder(tableId: Int, itemId: Int, ver: Int): Order = synchronized {
    if (tableId  < 1 || tableId > 100) throw new NoSuchOrderException("invalid table")
    if (!Menu.isExist(itemId)) throw new NoSuchOrderException("invalid item")

    val vKey = versionKey(tableId, itemId)
    // during the write, it should prevent other read while updating order
    rwLock.writeLock().lock()
    if (version.contains(vKey) && ver != version(vKey)) {
      // going to leave function, should unlock
      rwLock.writeLock().unlock()
      throw new ModifyException("old version")
    } else {
      version += (vKey -> (version.getOrElse(vKey, 0) + 1))
    }
    if (mStore.contains(tableId)) {
      if (mStore(tableId).contains(itemId)) {
        mStore(tableId)(itemId) = mStore(tableId)(itemId) + 1
      } else {
        mStore(tableId) += (itemId -> 1)
      }
    } else {
      mStore += (tableId -> Map[Int, Int](itemId -> 1))
    }
    rwLock.writeLock().unlock()
    new Order(tableId, itemId, mStore(tableId)(itemId), Menu.prepareTime(itemId), version(vKey))
  }

  // removeOrder : change the state of storage, only one thread can execute it at one time.
  @throws(classOf[ModifyException])
  @throws(classOf[NoSuchOrderException])
  override def removeOrder(tableId: Int, itemId: Int, ver: Int): Order = synchronized {
    if (tableId  < 1 || tableId > 100) throw new NoSuchOrderException("invalid table")

    if (mStore.contains(tableId)
      && mStore(tableId).contains(itemId)
      && mStore(tableId)(itemId) > 0
    ) {
      val vKey = versionKey(tableId, itemId)
      if (ver != version(vKey)) throw new ModifyException("old version")

      rwLock.writeLock().lock()
      var cnt = mStore(tableId)(itemId) - 1
      mStore(tableId)(itemId) = cnt

      version(vKey) = version(vKey) + 1
      rwLock.writeLock().unlock()
      new Order(tableId, itemId, cnt, Menu.prepareTime(itemId), version(vKey))
    } else {
      throw new NoSuchOrderException("invalid item")
    }
  }

  private def versionKey(table: Int, item: Int): String =  "%d_%d".format(table, item)

}
