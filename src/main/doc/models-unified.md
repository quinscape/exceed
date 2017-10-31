Context Models and Expressions 
==============================

Exceed uses a unified expression language embedded inside the JSON models to 
express the fine-details and "glue" of the application code. 

The expression language is simple javascript-ish language which is parsed
into a common abstract syntax tree format.

The language is used in many different contexts. Some context transpile it
to javascript to be executed, both on the client-side and on the server-side.

Other uses just transform the expression AST into another object representation,
for example a query expression is being transformed into a JOOQ query definition
to be queried from an SQL database.


Context Definitions
-------------------

Exceed allows you to define context variables on several model locations that
let you declare typed variables to use in your processes and views.

Here's an example from the *counter-example* process `counter.json`:

```json
{
    "context": {
        "properties": {
            "value": {
                "type": "Integer",
                "defaultValue": "0"
            },

            "limit": {
                "type": "Integer",
                "defaultValue": "10"
            }
        }
    },
    ...
}
```

The counter process has a variable `value` which contains the current counter value and another variable `limit` that
contains the limit at which the process will jump to the end view.

Context Types
-------------

Context Model Location                                                                        | Scope 
--------------------------------------------------------------------------------------------- | ----- 
`applicationContext` in [config model](./model-reference.html#xcd.config.ApplicationConfig)   | once
`sessionContext` in [config model](./model-reference.html#xcd.config.ApplicationConfig)       | per user session
`context` in each [process model](./model-reference.html#xcd.process.Process)                 | in that process
`context` in each [view model](./model-reference.html#xcd.view.View)                          | in that view 

The contexts combine to one set of valid identifiers per application location and must be unique within that location context.
Which means for example that there can't be no session context variable and process variable with the same name, 
but two processes can define the same process variable name since they're never in the same location.

Context variable references in client-side code are automatically detected and make the server provide the client with
the current data for that context variable. The client code can change context variables and the next server communication
will synchronize back the changes automatically.
