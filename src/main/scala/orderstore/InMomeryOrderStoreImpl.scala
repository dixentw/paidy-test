package orderstore

import menu.Menu
import scala.math.min
import scala.collection.mutable.Map
import java.util.concurrent.locks._

object InMemoryOrderStoreImpl extends OrderStore {

  private[this] var mStore: Map[Int, Map[Int, Int]] = Map()
  private[this] var version: Map[String, Int] = Map()
  private[this] val rwLock: ReadWriteLock = new ReentrantReadWriteLock()

  override def queryMenu(): Seq[Int] = {
    menu.Menu.getItems().map((t:(Int,Int)) => t._1)
  }

  override def queryOrderByTable(tableId: Int): Seq[Order] = {
    if (!mStore.contains(tableId)) Seq()
    else{
      rwLock.writeLock().lock()
      val res = mStore(tableId).map {
        case (item, count) => new Order(tableId,
              item,
              count,
              Menu.prepareTime(item),
              version.getOrElse(versionKey(tableId, item), 1))
      }.toSeq
      rwLock.writeLock().unlock()
      res
    }
  }

  override def addOrders(orders: (Int, Int, Int)*): Seq[Option[Order]] = {
    orders.map {
      case (table, item, ver) => {
        try {
          Some(incrOrder(table, item, ver))
        } catch {
          case _ : Exception => None
        }
      }
    }.toSeq
  }

  @throws(classOf[ModifyException])
  @throws(classOf[NoSuchOrderException])
  def incrOrder(tableId: Int, itemId: Int, ver: Int): Order = synchronized {
    if (tableId  < 1 || tableId > 100) throw new NoSuchOrderException("invalid table")
    if (!Menu.isExist(itemId)) throw new NoSuchOrderException("invalid item")

    val vKey = versionKey(tableId, itemId)
    if (mStore.contains(tableId)) {
      if (mStore(tableId).contains(itemId)) {
        if (ver != version(vKey)) throw new ModifyException("old version")
        rwLock.writeLock().lock()
        mStore(tableId)(itemId) = mStore(tableId)(itemId) + 1
        version(vKey) = version(vKey) + 1
        rwLock.writeLock().unlock()
      } else {
        rwLock.writeLock().lock()
        mStore(tableId) += (itemId -> 1)
        version += (vKey -> 1)
        rwLock.writeLock().unlock()
      }
    } else {
      rwLock.writeLock().lock()
      mStore += (tableId -> Map[Int, Int](itemId -> 1))
      version += (vKey -> 1)
      rwLock.writeLock().unlock()
    }
    new Order(tableId, itemId, mStore(tableId)(itemId), Menu.prepareTime(itemId), version(vKey))
  }

  @throws(classOf[ModifyException])
  @throws(classOf[NoSuchOrderException])
  override def removeOrder(tableId: Int, itemId: Int, ver: Int): Order = synchronized {
    if (tableId  < 1 || tableId > 100) throw new NoSuchOrderException("invalid table")

    if (mStore.contains(tableId) && mStore(tableId).contains(itemId)) {
      val vKey = versionKey(tableId, itemId)
      if (ver == version(vKey)) throw new ModifyException("old version")

      rwLock.writeLock().lock()
      var cnt = mStore(tableId)(itemId)
      cnt -= 1
      if (cnt == 0) mStore(tableId) -= itemId
      else mStore(tableId)(itemId) = cnt

      version(vKey) = version(vKey) + 1
      rwLock.writeLock().unlock()
      new Order(tableId, itemId, cnt, Menu.prepareTime(itemId), version(vKey))
    } else {
      throw new NoSuchOrderException("invalid item")
    }
  }

  private def versionKey(table: Int, item: Int): String =  "%d_%d".format(table, item)

}
