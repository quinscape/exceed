Exceed Type System
====================

The exceed type system is an extensible system based on a number of data atoms and
complex object structures built from them. The type system applies to many different
parts of exceed applications. Domain type definitions, context variable definitions but
also the runtime contents of data queries.

Property-Types
--------------

A property is a named property value, usually contained in a larger, if only organizational,
context. It is usually defined as JSON model.

```
{
    "name": "myProp",
    "type": "Integer"
}
```

The type of the property is defined by the type field and for some property types,
the typeParam and config fields.


Builtin Property Types
-----------------------

Type       | Description                                      | TypeParam                                      | Config Map
---------- | ------------------------------------------------ | ---------------------------------------------- | -------
Boolean    | boolean type                                     | -                                              | -
Currency   | currency type[*](#currency-type-limitations)     | Currency code                                  | -        
DataGraph  | [data graph structure](./data-graph.html)        | -                                              | -        
Date       | Date                                             | -                                              | -    
Decimal    | Decimal number                                   | -                                              | [decimalPlaces,precision,trailingZeroes](#decimal-config)    
DomainType | domain object value                              | Domain Type name                               | [implementation](#domaintype-config)        
Enum       | enum value                                       | Enum Type name                                 | [javaEnum](#enum-config)    
Integer    | 32 bit Integer number                            | -                                              | -    
List       | List                                             | Domain type or enum type or property type      | -    
Long       | 64 bit Integer number                            | -                                              | -    
Map        | map value with string keys                       | Domain type or enum type or property type      | -
Object     | any                                              | -                                              | -    
PlainText  | string                                           | -                                              | -        
RichText   | html text                                        | -                                              | -        
State      | state machine value                              | state machine name                             | -    
Timestamp  | timestamp                                        | -                                              | -        
UUID       | special UUID string type for ids                 | -                                              | -    

The typeParam values of *DomainType*, *Enum*, *State*, *List* and *Map* mean that the JSON property definitions are 
application specific as they can refer to application-specific type definitions by name.

The base extension defines some basic application domain types like *AppUser* and *AppTranslation* the application defines 
additional types to a final set of valid domain types, enum types and state machine types.

The system will also generate a domain type definition for every model class below the package de.quinscape.exceed.model.
The routing table model de.quinscape.exceed.model.routing.RoutingTable for example will have a corresponding domain type
definition with the type set to "xcd.routing.RoutingTable".


Property Type Configuration
---------------------------

Some property types offer additional configuration properties via an additional map property *config*. 

Config Example: 
   
```
{
    "name": "myValue",
    "type": "Decimal",
    "config": {
        "precision": 12,
        "decimalPlaces": 4
    }
}
```

This would define a *Decimal* property with a precision of 12 digits and 4 decimal places. The config.json contains
the application-wide defaults which are (precision=10 and decimalPlaces=3 by default).



Decimal Config
--------------

The *Decimal* type has the following config parameters:

 * decimalPlaces : Number of after the fractional separator
 * precision : Overall number of digits
 * trailingZeroes: *true* if the number is supposed to be formatted with trailing zeroes
 
There is additional application wide configuration in the config.json *decimalConfig* property.  

DomainType Config
-----------------

The *implementation* config parameter can be used to create a certain type of domain object, e.g. a special GeneratedDomainObject


Enum Config
-----------

The *javaEnum* config parameter can be used to make the property have java enum values of that type instead of ordinal values.
This is mostly useful and used for the model domain types.

Currency Type Limitations
-------------------------

Numbers in exceed come with the same limitations as javascript numbers. They're 64-bit IEEE-754 floating point numbers with limited precision.
Exceed offers the *Decimal* type for additional precision but uses a dedicated currency type for currency values.

The currency values are stored in one ten thousandth units. So 1.00 EUR would be a value of 10000. The *typeParam* field 
can be used to define the three letter currency code for that property. It can be left out to use application wide currency default.

Currency values also being normal Javascript numbers limits them to an interval of [MIN_SAFE_INTEGER, MAX_SAFE_INTEGER] for precise calculations as per 
[ECMAScript spec](https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER).

The range of of +/- 2^53 - 1 limits us to precise currency values in a range of +/- 90000 billion (10^13.95). For values 
larger or smaller then that, rounding errors will occur in the cent range.  
