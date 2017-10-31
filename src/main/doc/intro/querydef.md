The query definition expressions are the major example for a use of the unified exceed expression language that is not
transpiled to Javascript, be it server or client.

A query definition describes a data query process within the application domain which may any of the defined domain types within 
the application and their fields. The query expression string is parsed once into an abstract syntax tree and then evaluated
against the current context to produce a query definition object.

### Simple example:

```js
    query( OrderItem.as(oi) ).filter( oi.orderId == currentId )
```

This is query from the (shipping-app)[#url-missing] example. It queries all `OrderItem` objects with the alias `oi` and 
filter them by the order id in `current.id`. 

#### Simplified OrderItem model

```
{
    "name":"OrderItem",
    "properties":[
        {
            "name":"id",
            "type":"UUID",
            "required":true,
            "maxLength":36
        },
        {
            "name":"orderId",
            "type":"UUID",
            "required":true,
            "maxLength":36,
            "foreignKey":{
                "type":"Order"
            }
        }
    ]
}
```
Here is the corresponding domain type model in a simplified form. ( Original is under shipping-app/models/domain/OrderItem.json )

For the standard JOOQ query transformer, this would result in a query similar to

```sql
    SELECT * FROM shipping_app.order_item oi where oi.order_id == '145bf30d-43ae-4e94-9607-ea8a9acab359' 
```
---

## Query Definition methods

