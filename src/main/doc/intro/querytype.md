The query domain type definition is embedded within the query definition. It contains the definition of the actual
domain types and properties/fields involved. The query definition then configures the query, the filter, how many rows, 
at what offset etcpp.

Query domain types are most easily referenced by using their name as an identifier like in the OrderItem example above.
Query domain types can be joined with other query domain types.

```js
    query( Customer.as(c).join( Address.as(a)).on( c.addressId == a.id) ) 
``` 
---

## Query Domain Type methods

