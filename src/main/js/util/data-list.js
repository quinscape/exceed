var immutableUpdate = require("react-addons-update");
var assign = require("object.assign").getPolyfill();

const ROOT_NAME = "[DataListRoot]";

function createPropertyLookup(types)
{
    var propertyLookup = {};

    for (var typeName in types)
    {
        if (types.hasOwnProperty(typeName))
        {
            var type = types[typeName];
            var properties = type.properties;
            for (var i = 0; i < properties.length; i++)
            {
                var property = properties[i];
                propertyLookup[typeName + "." + property.name] = property;
            }
        }
    }

    return propertyLookup;
}

/**
 * Encapsulates a data-list structure and provides immutable update function on it.
 *
 * @param dataList
 * @param onChange
 * @constructor
 */
function DataList(dataList, onChange)
{
    this.types = dataList.types;
    this.enums = dataList.enums;
    this.columns = dataList.columns;
    this.rows = dataList.rows;
    this.rowCount = dataList.rowCount;
    this.propertyLookup = createPropertyLookup(this.types);
    this.onChange = onChange;
}

DataList.prototype.getRaw = function (key)
{
    return {
        types: this.types,
        enums: this.enums,
        columns : this.columns,
        rows: this.rows,
        rowCount: this.rowCount
    }
};

DataList.prototype.lookupProperty = function (key)
{

    var property = this.propertyLookup[key];
    if (!property)
    {
        throw new Error("Column '" + key + "' not found in data list");
    }

    return property;
};

DataList.prototype.getColumnType = function (column)
{
    var e = this.columns[column];
    if (!e)
    {
        throw new Error("No column '" + column + "' in dataList columns ( '" + JSON.stringify(this.columns) + " )");
    }
    return this.lookupProperty(e.type + "." + e.name);
};

DataList.prototype.getCursor = function (path)
{
    validatePath(path);

    if (typeof path[0] !== "number")
    {
        throw new Error("First key path entry must be a numeric row index");
    }

    var type = ROOT_NAME, typeParam = null;

    if (path.length > 1)
    {
        var column = path[1];

        var property = this.getColumnType(column);
        type = property.type;
        typeParam = property.typeParam;

        if (path.length > 2)
        {
            var typeInfo = this.followTypeDefinitionPath(path, 2, type, typeParam);
            type = typeInfo.type;
            typeParam = typeInfo.typeParam;
        }


        if (type === "DomainType")
        {
            type = typeParam;
            typeParam = null;
        }
    }

    return new DataListCursor(this, path, type, typeParam);
};


DataList.prototype.followTypeDefinitionPath = function(path, startIndex, type, typeParam, resolve)
{
    var key, lookupKey, property = null, parent;

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

            lookupKey = type + "." + key;
            property = this.propertyLookup[lookupKey];
            if (!property)
            {
                throw new Error("Cannot find property for '" + lookupKey + "'");
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
                type: type,
                dataList: this
            };
        }
        else
        {
            return assign({
                parent: parent,
                dataList: this
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
};

function walk(obj, path)
{
    //console.log("WALK", obj, path);

    for (var i = 0; i < path.length; i++)
    {
        //console.log("W", obj, path[i]);
        obj = obj[path[i]];
    }
    return obj;
}

DataList.prototype.resolveProperty = function (path, startIndex, type, typeParam)
{
    return this.followTypeDefinitionPath(path, startIndex, type, typeParam, true);
};

function validatePath(path)
{
    if (!path || typeof path !== "object" || !path.length)
    {
        throw new Error("Invalid path: "+ JSON.stringify(path));
    }
}


function DataListCursor(dataList, path, type, typeParam)
{
    validatePath(path);

    this.value = walk(dataList.rows, path);
    this.dataList = dataList;
    this.type = type;
    this.typeParam = typeParam;
    this.path = path;
}

DataListCursor.prototype.get = function (path)
{
    path = path && path.length ? this.path.concat(path) : this.path;

    var obj = this.dataList.rows;

    for (var i = 0; i < path.length; i++)
    {
        obj = obj[path[i]];
    }
    return obj;
};

// Create an immutable update method for every command supported by "react-addons-update"
(function (cursorProto)
{
    var operations = [
        "push",
        "unshift",
        "splice",
        "set",
        "merge",
        "apply"
    ];

    function spec(path, op, value)
    {
        var root = {};
        var spec = root;

        for (var i = 0; i < path.length; i++)
        {
            spec = spec[path[i]] = {};
        }

        spec[op] = value;

        //console.log("SPEC", JSON.stringify(root));

        return root;
    }

    function createOperand(op)
    {
        return function (path, value)
        {
            path = path && path.length ? this.path.concat(path) : this.path;
            this.update(spec(
                path,
                "$" + op,
                value
            ), path);
        }
    }

    for (var i = 0; i < operations.length; i++)
    {
        var op = operations[i];
        cursorProto[op] = createOperand(op);
    }

})(DataListCursor.prototype);

DataListCursor.prototype.update = function (spec, path)
{
    var newRows = immutableUpdate(this.dataList.rows, spec);
    this.dataList.onChange.call(this, newRows, path);
    this.dataList.rows = newRows;
};

DataListCursor.prototype.pop = function (howMany)
{
    return this.dataList.getCursor(this.path.slice(0, this.path.length - ( howMany || 1)));
};

DataListCursor.prototype.getCursor = function (path)
{
    return this.dataList.getCursor(this.path.concat(path));
};

DataListCursor.prototype.getDomainObject = function (type)
{
    var object;

    var rowIndex = this.path[0];
    var dataList = this.dataList;

    var row = dataList.rows[rowIndex];

    if (this.path.length == 1)
    {
        object = {};

        var cols = dataList.columns;
        if (!cols)
        {
            throw new Error("No cols");
        }
        var checkImplicit = false;

        for (var name in cols)
        {
            if (cols.hasOwnProperty(name))
            {
                var entry = cols[name];

                if (!type)
                {
                    type = entry.type;
                    checkImplicit = true;
                }

                if (entry.type === type)
                {
                    object[entry.name] = row[name];
                }
                else if (checkImplicit)
                {
                    throw new Error("Implicit type detection failed: DataList contains '" + type + "' and '" + entry.type + "' objects");
                }
            }
        }
        var typeDef = dataList.types[type];
        object._type = typeDef.name;
        return object;
    }
    else
    {
        var propType = this.getPropertyType();
        if (propType.type === "List")
        {
            throw new Error("Cannot extract single domain object from List");
        }
        else if (propType.type === "Map")
        {
            throw new Error("Cannot extract single domain object from Map");
        }
        else if (propType.type === "DomainType")
        {
            object = assign({
                _type: propType.typeParam
                },
                this.value
            );
        }
        else
        {
            object = assign({
                _type: propType.type
            }, this.value);
        }

        if (type && type !== object._type)
        {
            throw new Error("Type parameter requests '" + type + "' but cursor points to '" + object._type + "'");
        }

        return object;
    }
};


DataListCursor.prototype.getPropertyType = function (path)
{
    var property;

    var type = this.type;
    var typeParam = this.typeParam;

    if (!path || !path.length)
    {
        type = ROOT_NAME;
        typeParam = null;
        path = this.path.slice(1);
    }

    if (type === ROOT_NAME)
    {
        var column = path[0];
        var e = this.dataList.columns[column];
        if (!e)
        {
            throw new Error("No column '" + column + "' in dataList columns ( '" + JSON.stringify(this.columns) + " )");
        }
        property =  this.dataList.lookupProperty(e.type + "." + e.name);

        if (path.length == 1)
        {
            return assign({
                parent: e.type,
                dataList: this.dataList
            }, property);
        }

        //console.log("property", property, key);
        type = property.type;
        typeParam = property.typeParam;
    }

    //console.log("getPropertyType start", type, typeParam)
    return this.dataList.resolveProperty(path, 1, type, typeParam);

};

DataListCursor.prototype.valueOf = function ()
{
    return this.value;
};

DataListCursor.prototype.toString = function ()
{
    return String(this.value);
};


DataList.Cursor = DataListCursor;

module.exports = DataList;
