module.exports = function (obj)
{
    if (obj !== Object(obj))
    {
        throw new TypeError('Object.keys called on a non-object');
    }

    var values = [], name;
    for (name in obj)
    {
        if (Object.prototype.hasOwnProperty.call(obj, name))
        {
            values.push(obj[name]);
        }
    }
    return values;
};
