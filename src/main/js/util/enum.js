"use strict";

const keys = require("./keys");

/**
 * Create a new Enum type based on the given map.
 *
 * The values in the map are irrelevant, they get replaced by the key as 
 * per js enum pattern.
 *
 * @param map
 * @returns {Enum}
 * @constructor
 */
function Enum(map)
{
    if (!(this instanceof Enum))
    {
        return new Enum(map);
    }

    for (var name in map)
    {
        if (map.hasOwnProperty(name))
        {
            this[name] = name;
        }
    }
}

Enum.prototype.values = function()
{
    var l=[];
    for (var name in this)
    {
        if (this.hasOwnProperty(name))
        {
            l.push(name);
        }
    }
    return l;
};

Enum.prototype.isValid = function(value)
{
    return value && this.hasOwnProperty(value) && this[value] === value;
};

Enum.prototype.equals = function(a, b)
{
    if (!this.isValid(a))
    {
        throw new Error( a + " is not a valid enum value in " + keys(this).join(", "));
    }

    if (!this.isValid(b))
    {
        throw new Error( b + " is not a valid enum value in " + keys(this).join(", "));
    }

    return a === b;
};



module.exports = Enum;
