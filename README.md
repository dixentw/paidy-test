# paidy-test

## Assumptions
* tables from 1..100
* fixed length of menu items, say 1..50
* fixed prepare time for each menu item (5-15min), generated at menu creating time.
* Menu can be repeated ordered for one table, and no count limit.

## Design Decisions
1. scala doesn't come with standard http library
    - server/client communicate through simple api call
    - server api should be easy to translate to REST call
1. for effecient query the orders, I adopt optimistic lock concept. While query the orders from server, server respond with version key, implemented as integer. When client modify one order, if the order has been updated, the version key will be incremented, therefore the modification will throw exception.
 

## Server
1. Singletone
1. Interface: `OrderStore`, Implementation: `InMemeryOrderStoreImpl`
1. queryMenu
    * single source of truth, client should query the menu first before place the order.
1. queryOrderByTable
    * return current orders of given table in storage.
    * it returns items that already count as 0 with api key for client that need to add/remove the order.
2. addOrders take varargs of tuple (table number, item id, version key), return the list of execute result.
    * if corresponding position of result list is valid order, then the add is successful
    * else that add is fail
3. incrOrder take (table number, item id, version) to perform add item to table.
    * the actual implementation of addOrders
    * throws ModifyException if the version key is incorrect
    * throws NoSuchElementException if the table is invalid
4. removeOrder take (table number, item id, version) to perform remove item from table.
    * input (table number)
    * throws ModifyException if the version key is incorrect
    * throws NoSuchElementException if the table is invalid, menu item is not in store.

## Client
1. Implemntation: `Scenarios`
    1. Implemented with heavily conflict condition (all orders are the modification of same table and item)
        1. actions: add order and assert order incremented by 1, remove order and assert order decremented by 1.
    1. Implemented with heavily conflict condition (all orders are the modification of same table and item)
        1. actions: add constant time of same orders to system
        1. assert the order should incremented by given constant number.

## Execution
1. install `sbt`
1. `run` for multithread test.
1. `test` for unit test of implementation.


