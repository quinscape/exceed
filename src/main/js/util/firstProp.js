/**
 * Returns the name of first property that happens to show up in iteration order.
 *
 * @param obj
 * @returns {?string}    property name or null
 */
module.exports = function (obj)
{
    for (var name in obj)
    {
        if (obj.hasOwnProperty(name))
        {
            return name;
        }
    }
    return null;
};
