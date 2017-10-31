Field expressions are currently always converted to JOOQ conditions which is not optimal
as it forces alternate query transformers to deal with JOOQ conditions.

We might introduce our own condition abstraction later if we find compelling use
cases or volunteers for implementation.

### Examples


```js
    name == 'Smith' || value > 5
```

```js
    Order.customerId == Customer.id 
```

```js
    o.customerId != c.id 
```

The field expressions use the normal expression syntax plus some additional operations, both
to service the needs of our models or just relayed to the JOOQ Field class. 

The meaning of the field names depends on the surrounding query context. Field
references must be unambiguous. Fully qualified field references use either
the domain type name as prefix or the alias defined for the domain type.
