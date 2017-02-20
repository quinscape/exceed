/**
 * Produces an immutable copy of the given object excluding the given property name.
 *
 * @param obj           object
 * @param exclude       name of excluded property
 * @returns {{}}
 */
module.exports = function (obj, exclude)
{
    var newObject = {};
    for (var name in obj)
    {
        if (name !== exclude && obj.hasOwnProperty(name))
        {
            newObject[name] = obj[name];
        }
    }

    //console.log("OMIT(", obj, exclude, ") => ", newObject);

    return newObject;
};
