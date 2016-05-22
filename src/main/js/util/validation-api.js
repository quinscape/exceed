"use strict";

var assign = require("object.assign").getPolyfill();

var i18n = require("../service/i18n");

const DEFAULTS = {
    required: false,
    type: "string",
    maxLength: -1
};

var typeValidators = {};


function createRequiredValidator(type, path)
{
    return function(value)
    {
        if (value)
        {
            return {
                valid: true,
                value: value
            }
        }
        else
        {
            return {
                valid: false,
                message: i18n("Required:" + type + "." + path),
                value: ""
            };
        }
    };
}

function createMaxLengthValidator(type, path, maxLength)
{
    return function(value)
    {
        if (value.length > maxLength)
        {
            return {
                valid: false,
                message: i18n("TooLong:" + type + "." + path),
                value: value
            };
        }
        else
        {
            return {
                valid: true,
                value: value
            }
        }
    };
}

function createNumberValidator(type, path, value)
{

    return function (value)
    {
        var valueAsNumber = +value;
        if (isNaN(valueAsNumber))
        {
            return {
                valid: false,
                message: i18n("InvalidNumber:" + type + "." + path),
                value: value
            }
        }
        else
        {
            return {
                valid: true,
                value: valueAsNumber
            }
        }
    }
}

function errorMessageProlog(type, name)
{
    return "Error in type definition '" + type + "', property '" + name +"':";
}

/**
 * Imports and validates a rule.
 *
 * @param domainType        domain type
 * @param name              property name
 * @param rule              property rule
 *
 */
function importRule(domainType, name, rule)
{
    var ruleType = rule.type;
    var maxLength = rule.maxLength;
    var required = rule.required;

    if (typeof ruleType !== "string")
    {
        throw new Error( errorMessageProlog(domainType, name) + " property has no type.");
    }

    var validators = [];

    if (required)
    {
        validators.push(createRequiredValidator(type, name));
    }

    if (ruleType === "Integer" || ruleType === "Long")
    {
        validators.push(createNumberValidator(type, name));
    }

    if (typeof maxLength === "number" && maxLength >= 1)
    {
        validators.push(createMaxLengthValidator(type, name, maxLength));
    }

    return validators;
}

function importRules(type, rules)
{
    //console.log("import", type, rules);

    var out = {};
    for (var name in rules)
    {
        if (rules.hasOwnProperty(name))
        {
            out[name] = importRule(type, name, rules[name])
        }
    }
    return out;
}

var domainRules = {};

// import domain.json ignoring the '@' comment
var domainJSON = require("../domain.json");
for (var type in domainJSON)
{
    if (type !== '@' && domainJSON.hasOwnProperty(type))
    {
        domainRules[type] = importRules(type, domainJSON[type]);
    }
}

console.log("DOMAIN-RULES", domainRules);

module.exports = {
    registerTypeValidator: function(type, fn)
    {
        var array = typeValidators[type];
        if (array)
        {
            array.push(fn);
        }
        else
        {
            array = typeValidators[type] = [ fn ];
        }
    },
    registerValidator: function(type, path, fn)
    {
        if (typeof fn !== "function")
        {
            throw new Error("Validator must be a function")
        }

        var typeRules = domainRules[type];
        if (!typeRules)
        {
            throw new Error("Unknown type: " + type);
        }

        var array = typeRules[path];
        if (!array)
        {
            array = typeRules[path] = [ fn ];
        }
        else
        {
            array.push(fn);
        }
    },
    validateType: function (type, value)
    {
        var array = typeValidators[type];
        if (array && array.length)
        {
            for (var i = 0; i < array.length; i++)
            {
                var result = array[i].call(null, value);
                if (!result.valid)
                {
                    return result;
                }
            }
        }
        return {
            valid: true,
            message: ""
        };
    },
    validate: function (type, path, value)
    {
        //console.log("validate ", type, path, value);

        var typeRules = domainRules[type];
        if (!typeRules)
        {
            throw new Error("Unknown type: " + type);
        }
        var array = typeRules[path];
        if (array && array.length)
        {
            for (var i = 0; i < array.length; i++)
            {
                var result = array[i].call(null, value);
                if (!result.valid)
                {
                    return result;
                }
                else
                {
                    value = result.value !== undefined ? result.value : value;
                }
            }
        }

        return {
            valid: true,
            message: "",
            value: value
        };
    },
    registerType: function(type, rules)
    {
        domainRules[type] = importRules(type, rules);
    }
};
