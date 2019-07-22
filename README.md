# paidy-test

## Design Decisions
1. scala doesn't come with standard http library
    - server/client communicate through simple api call
    - server api should be easy to translate to REST call

## assumptions
* tables from 1..100
* fixed length of menu items, say 1..200
* fixed prepare time for each menu item (5-15min)

### Server - singleton
1. init
    * generate menu and prepare time
1. query menu
    * single source of truth, client should query the menu first to place order.
2. add order (table number, item id...)
    * output: boolean indicating successful
3. remove order (table number, item id)
    * output: boolean indicating successful
    * remain zero item, don't want to remove the version key
4. query orders
    * input (table number)
    * output item, count of that item, prepare time

### Client
1. query the menu
2. add order
    * randomly choose menu items
    * randomly choose a table
3. remove order
4. query order
5. how to perform this action make test if it correct?
