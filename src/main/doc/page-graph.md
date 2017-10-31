DataGraph Structure
-------------------

In addition to the [complex property types (`List`,`Map` and `DomainType`)](./types.html) we support another, partially self-describing
data structure as output format of our data queries which we call data graph. 

On the java side the data graph is represented as de.quinscape.exceed.runtime.component.DataGraph instance, for Javascript
it is a plain JSON structure.

Example:

```
{
    "type" : "OBJECT" | "ARRAY",
    "columns" : <map of column descriptors>,
    "rootObject" : <object or array>,
    "count" : <row count>,
    "qualifier" : [qualifier-string]
}
```

The `type` property contains either the value "OBJECT" or "ARRAY", describing the nature of the `rootObject` object and the exact
meaning of the `column` map which contains property models in any case.


ARRAY Graphs
------------

For data graphs of type "ARRAY", the root object is an array of complex objects. Each property of those complex objects has
an entry in the columns map.

 
```
{
    "type" : "ARRAY",
    "columns": {
        "name": {
            "name": "name",
            "type": "PlainText",
            "domainType": "Foo",
            "data": 1,
            "config" : {
                "queryName" : "q"
            }
        },
        "num": {
            "name": "num",
            "type": "Integer",
            "domainType": "Foo",
            "data": 2,
            "config" : {
                "queryName" : "q"
            }
        }
    },
    "rootObject": [
        {
            "name": "TestFoo",
            "num": 123
        },
        {
            "name": "TestFoo #2",
            "num": 234
        }
    ],
    "count" : 2
}
```

The columns map describes two properties `name` and `num` which are `PlainText` and `Integer` respectively. For queries, 
the column property will also contain a `domainType` field to mark the property as originally belonging to that domain 
type. The name field will reflect the originalname within the domain type in that case (while the columns key always 
describes the column name as it is for the current `rootObject`).

Query graphs will also contain an `queryName` config value describing the type name or alias the column belongs to 
within the query (if the query executor supports that).  

In case of a paginated result graph, the `count` field contains the number of result rows available.

OBJECT graphs
-------------

Data graphs of type "OBJECT" have a single complex object as `rootObject`. They can have a single column "*" in which
case all properties of the object will be of that type (Java Map<String,T> equivalent). Otherwise each column entry has 
its own type identified by its name. It is basically an unnamed ad-hoc complex object -- as opposed to the domain types 
themselves which are named complex object definitions.  


Self-describing
---------------

A data graph that only contains simple properties is self-describing in terms of exceed types in any exceed application. 
It already might reference application specific domain types in its `domainType` attributes though. The structure of the 
rootObject is still unambiguous though.  

A data graph might also contain embedded domain objects of an application-specific domain type. The structure of a 
`rootObject` with embedded application-specific domain types is only known within that application.

In so far you should proceed with utmost care if you exchange data graphs between different applications or application
versions. Make sure that the domain types contained really mean what you think they mean.
