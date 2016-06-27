const immutableUpdate = require("react-addons-update");
const assign = require("object.assign").getPolyfill();
const util = require("./datalist-util");

const converter = require("../service/property-converter");

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

function DataListCursor(dataList, path, type, typeParam, isProperty)
{
    util.validatePath(path);

    this.dataList = dataList;
    this._value = null;
    this.rows = null;
    this.type = type;
    this.typeParam = typeParam;
    this.path = path;
    this.property = isProperty;
}

DataListCursor.prototype.isProperty = function ()
{
    return this.property;
};

DataListCursor.prototype.get = function (path)
{
    var p = this.path;

    // if we have no path, it's our value property
    if (path && path.length)
    {
        p = p.concat(path);
        return walk(this.dataList.rows, p);
    }
    else
    {
        var currentRows = this.dataList.rows;
        if (this.rows === currentRows)
        {
            return this._value;
        }

        this.rows = currentRows;
        var value = walk(this.dataList.rows, p);

        this._value = value;

        return value;
    }
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
    this.dataList.onChange.call(this.dataList, newRows, path);
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

/**
 * Extracts a domain object from the location the cursor currently points to. This can be either a domain object or
 * a data list root with mixed types.
 *
 * The method will fail if the cursor currently points to a property.
 *
 * @param type
 * @returns {*}
 */
DataListCursor.prototype.getDomainObject = function (type)
{
    var object;

    var rowIndex = this.path[0];
    var dataList = this.dataList;

    var row = dataList.rows[rowIndex];

    if (this.path.length == 1)
    {
        // extract from data list root
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
                var property = cols[name];

                if (!type)
                {
                    type = property.domainType;
                    checkImplicit = true;
                }

                if (property.domainType === type)
                {
                    object[property.name] = row[name];
                }
                else if (checkImplicit)
                {
                    throw new Error("Implicit type detection failed: DataList contains '" + type + "' and '" + property.type + "' objects");
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
                this.get()
            );
        }
        else
        {
            object = assign({
                _type: propType.type
            }, this.get());
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
        type = util.ROOT_NAME;
        typeParam = null;
        path = this.path.slice(1);
    }

    if (type === util.ROOT_NAME)
    {
        var column = path[0];
        property = this.dataList.columns[column];

        if (path.length == 1)
        {
            return assign({
                parent: property.domainType,
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
    return this.get();
};

DataListCursor.prototype.toString = function ()
{
    return String(this.get());
};


module.exports = DataListCursor;
