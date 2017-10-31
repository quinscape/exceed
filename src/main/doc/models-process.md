Process Models
=============

Process models describe activity-diagram like processes within an application.

~~ProcessStructure~~

Processes
---------

```json
{
    "startTransition": {
        "to": "main"
    },
    
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
    
    "states": {
    
        "main": {
            "type": "xcd.process.ViewState",
            "transitions": {
                "inc": {
                    "to": "check",
                    "action": "value = value + 1"
                }
            }
        },
        
        "check": {
            "type": "xcd.process.DecisionState",
            "decisions": [
                {
                    "expression": "value >= limit",
                    "transition": {
                        "to": "end",
                        "action" : "syslog('Jumping to End')"
                    }
                }
            ],
            "defaultTransition": {
                "to": "main",
                "discard": true
            }
        },
        
        "end": {
            "type": "xcd.process.ViewState"
        }
    }
}
```
The `states` map of a property model contains a named set of process state models. Each process state model must be 
identified by a `type` property which currently has be be one of:

    * xcd.process.ViewState  
    * xcd.process.DecisionState  
    * xcd.process.SubprocessState  
    * xcd.process.EndState  
