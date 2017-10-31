Domain Models
=============

The domain models describe the business objects of the application. 

~~DomainStructure~~

DomainTypes
-----------

The normal [domain types](./model-reference.html#xcd.domain.type.DomainTypeModel) ( in this example `/models/domain/DomainTypeA.json`), 
are the core of exceed domain models.   

Here we see a simple definition of our example domain type `DomainTypeA`

```json
{
    "name":"DomainTypeA",
    "properties":[
        {
            "name":"id",
            "type":"UUID",
            "required":true,
            "maxLength":36
        },
        {
            "name":"name",
            "type":"PlainText",
            "required":true,
            "maxLength":64
        },
        {
            "name":"number",
            "type":"Integer",
            "required":true,
            "defaultValue":"0"
        },
        {
            "name":"ownerId",
            "type":"UUID",
            "maxLength":36,
            "foreignKey":{
                "type":"AppUser"
            }
        }
    ]
}
```

`DomainTypeA` defines 4 properties ( `id`, `name`, `num` and `ownerId`) with different property types. Currently, all
domain types use `UUID` values as primary key and each domain type must have an `UUID` property with the same `id`. 

The `name` property is a `PlainText` / string property with a maximum of 64 characters. Note that while exceed itself
doesn't really care about the maxLength much besides when its validating values, but the default SQL storage will 
use `varchar(n)` up to 256 characters and for more than that `text`. 

`number` is an `Integer` property which is also marked `required` / `not null`, just like `name`.

The `ownerId` property is marked as being a foreign key property, which means that it connects one object with another.
In this case `orderId` points to the `AppUser` that is its owner. `AppUser` is one of the base domain objects. 


Enum Types
----------

[Enum types](./model-reference.html#xcd.domain.type.EnumType) contain a list of named enum constants. They are stored as ordinal numbers. 

```json
{
    "name": "MyEnum",
    "values" : ["A", "B", "C", "D"]
}
```
Within the exceed expression language the enum type names can be used as identifiers. `MyEnum.A` is the enum constant `A`.

For an expression like `myValue == MyEnum.B` is is made sure thae `myValue` actually is a enum variable of the same type.  

State Machines
--------------

[State machines](./model-reference.html#xcd.state.StateMachine) contain a number of named states, each defining a number of valid transitions to other states.

Here is the `OrderStatus` state machine from the shipping-app example:
 
```json
{
    "name" : "OrderStatus",
    "startState": "ACCEPTED",
    "states": {
        "ACCEPTED": [
            "READY",
            "CANCELED"
        ],
        "READY": [
            "SENT",
            "CANCELED"
        ],
        "SENT": [
            "DELIVERED",
            "LOST_ON_DELIVERY"
        ],
        "DELIVERED": [
            "PAID",
            "RETURNED",
            "LOST_ON_RETURN"
        ],
        "RETURNED": [],
        "LOST_ON_DELIVERY": [],
        "LOST_ON_RETURN": [],
        "PAID": [],
        "CANCELED": []
    }
}
```

The state machine starts in "ACCEPTED", from which it either goes to "READY" or "CANCELED" and so forth. The current state
of a state machine can be attached to a domain property using the `State` property type.

The states can be type-safely compared using the expression language and offer a `from` method.

`OrderStatus.PAID.from(myStatus)` evaluates to true if there is a valid transition from the state in `myStatus` to
`PAID`.

There is a helper component [StateMachineButtons](./component.html#Component-StateMachineButtons) that renders groups
of transition buttons based on the current state. The `shipping-app` example does this in "orders/detail"

Query type models
-----------------

So far, we've kept the query language for the domain objects as simple as possible which means that it is lacking
the a lot of the functions that SQL offers. For this purpose, we can interface with normal SQL queries. We just
need to know how the result set looks like.

Here is an example of a [query type model](./model-reference.html#xcd.domain.type.QueryTypeModel) from the shipping app. It queries the paid orders for the last *n* months.

```json
{
    "query" : "select date_trunc('month', accepted) as month, sum(\"sum\") from shipping.\"order\" where date_trunc('year', accepted) = date_trunc('year', current_date) group by month order by month;",
    "count" : "12",
    "columnTypes" : [
        {
            "name" : "month",
            "type":"PlainText"
        },
        {
            "name" : "sum",
            "type" : "Currency"
        }
    ]
}
```

The `query` property contains the SQL query. The `columnTypes` property defines the property types of the result set.

### Parametrized Query Types

Here we see another example that select an average value a "num" column grouped by an enum type.

```json
{
    "query" : "SELECT type, avg(num) from foo where type = ? group by type order by type",
    "count" : "1",
    "parameterTypes" :[
        {
            "name" : "type",
            "type":"Enum",
            "typeParam":"MyEnum",
            "defaultValue" : "1"
        }
    ],
    "columnTypes" : [
        {
            "name" : "fooType",
            "type":"Enum",
            "typeParam":"MyEnum"
        },
        {
            "name" : "sumOfNums",
            "type" : "Decimal"
        }
    ]
}
```

The SQL query uses the normal `?` place holders for the SQL parameters and the `parameterTypes` property defines the 
type of all SQL parameters.

The `count` field is an expressions that provides the column count available to the query. There can also be a property
`countQuery` that defines an additional query to provide that count.

### Domain Types vs Query Types

The normal use case is to declare your domain in terms of these domain objects
and work with that. Query type models (see below) currently refer to the same data sources that are used by the normal
domain types. In the future we might introduce handling multiple database connections to allow the usage of query types
in integrative scenarios where an exceed application interfaces with an external database schema.

DB Support
----------

Each domain type used a StorageConfiguration interface to do the actual data querying and updating and define naming 
strategies etc. The default storage configuration is `jooqDatabaseStorage` which uses JOOQ and Spring JDBC to access
[JOOQ compatible Databases](https://www.jooq.org/doc/3.9/manual/reference/supported-rdbms/). The synchronization of 
the data base schema with the model happens via the ANSI *information_schema* functionality which not all databases 
support. Over time we might write other schema synchronization implementations.

For now, the best tested Database to use with exceed is clearly Postgresql. Over time we will add tests for more databases
but we will clearly be in a situation for a while were we have two different classes of support for databases:

 * "Gold" level where the database supports the full-range of functionality including schema management 
 * "Silver" level for databases that can be made compatible by providing the just the kind of schema that corresponds
   through the model so we can run exceed application without neccessarily being able to create the schemas for all 
   database types.


Models as Domain Types
----------------------

The models are parsed into a hierarchy of java POJOs which are then composed to the final application model in memory as
java objects including an internal meta model containing prepared working data based on the analysis of the models. Each
of this models has a natural JSON structure corresponding to the Java structure defining its schema.

For model editing purposes, there exists another view on the model hierarchy as domain objects. Each domain type model
has a system domain type definition equivalent within the exceed system.

The type corresponds to the relative package path to *de.quinscape.exceed.model* with an constant "xcd." prefix.


 * Java class de.quinscape.exceed.model.view.View -> Domain type `xcd.view.View` 
 * Java class de.quinscape.exceed.model.routing.RoutingTable -> Domain type `xcd.routing.RoutingTable` 




