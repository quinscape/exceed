const assign = require("object-assign");

const Enum = require("./enum");
const DataGraphType = new Enum({
    ARRAY: true,
    OBJECT: true
});

const WILDCARD_SYMBOL = "*";

function followTypeDefinitionPath(dataGraph, path, startIndex, type, typeParam, resolve)
{
    var key, property = null, parent;

    for (var i = startIndex; i < path.length; i++)
    {
        key = path[i];

        //console.log("key = ", key);

        if (type === "List")
        {
            if (typeof key !== "number")
            {
                throw new Error("List indices must be numeric:" + path)
            }

            parent = type;
            type = typeParam;
            typeParam = null;
            property = null;
        }
        else if (type === "Map")
        {
            parent = type;
            type = typeParam;
            typeParam = null;
            property = null;
        }
        else
        {
            if (type === "DomainType")
            {
                parent = typeParam;
                type = typeParam;
                typeParam = null;
                property = null;
            }

            property = dataGraph.getPropertyModel(type, key);
            if (!property)
            {
                throw new Error("Cannot find property for '" + type + ":" + key + "'");
            }

            parent = type;
            type = property.type;
            typeParam = property.typeParam;
        }

        //console.log("TYPE", type, typeParam, "PARENT", parent, "PROPERTY", property);
    }

    if (resolve)
    {
        if (!property)
        {
            return {
                parent: parent,
                type: type
            };
        }
        else
        {
            return assign({
                parent: parent
            }, property);
        }
    }
    else
    {
        return {
            type: type,
            typeParam: typeParam
        };
    }
}

module.exports = {
    ROOT_NAME: "[DataGraphRoot]",
    validatePath: function (path)
    {
        if (!path || typeof path !== "object" || typeof path.length !== "number")
        {
            throw new Error("Invalid path: " + JSON.stringify(path));
        }
    },
    DataGraphType : DataGraphType,
    WILDCARD_SYMBOL: WILDCARD_SYMBOL,
    walk: function (obj, path)
    {
        //console.log("WALK", obj, path);

        try
        {
            var len = path.length;
            for (var i = 0; i < len; i++)
            {
                //console.log("W", obj, path[i]);
                obj = obj[path[i]];
            }
            return obj;
        }
        catch (err)
        {
            console.error("Error walking ", obj, path, err);
            throw new Error("Walking error");
        }
    },
    followTypeDefinitionPath: followTypeDefinitionPath
};
