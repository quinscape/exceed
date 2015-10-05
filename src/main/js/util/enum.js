"use strict";

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
    return this.hasOwnProperty(value) && this[value] === value;
};

module.exports = Enum;
