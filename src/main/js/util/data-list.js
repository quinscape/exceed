const assign = require("object.assign").getPolyfill();
const DataListCursor = require("./data-list-cursor");

const util = require("./datalist-util");
const keys = require("./keys");


/**
 * Encapsulates a data-list structure and provides immutable update function on it.
 *
 * @param types         map of domain types for the application
 * @param dataList      raw data list JSON or DataList instance
 * @param onChange      change callback
 * @constructor
 */
function DataList(types, dataList, onChange)
{
    this.columns = dataList.columns;
    this.rows = dataList.rows;
    this.rowCount = dataList.rowCount;

    if (dataList instanceof DataList)
    {
        this.types = dataList.types;
        this.onChange = onChange || dataList.onChange;
    }
    else
    {
        this.types = types;
        this.onChange = onChange;
    }
}

DataList.prototype.update = function (rawList)
{
    this.columns = rawList.columns;
    this.rows = rawList.rows;
    this.rowCount = rawList.rowCount;

    this.onChange.call(this, rawList.rows, null);
};

DataList.prototype.getRaw = function ()
{
    return {
        columns : this.columns,
        rows: this.rows,
        rowCount: this.rowCount
    }
};


DataList.prototype.getColumnType = function (column)
{

    var property = this.columns[column];
    if (!property)
    {
        throw new Error("No column '" + column + "' in dataList columns ( '" + JSON.stringify(keys(this.columns)) + " )");
    }
    return property;

};

DataList.prototype.getCursor = function (path)
{
    util.validatePath(path);

    if (typeof path[0] !== "number")
    {
        throw new Error("First key path entry must be a numeric row index");
    }

    var type = util.ROOT_NAME, typeParam = null, isProperty = false;

    if (path.length > 1)
    {
        isProperty = true;
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

        if (type === "Map" || type === "List" || this.types[type])
        {
            isProperty = false;
        }
    }

    return new DataListCursor(this, path, type, typeParam, isProperty);
};


DataList.prototype.followTypeDefinitionPath = function(path, startIndex, type, typeParam, resolve)
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

            property = this.getPropertyModel(type, key);
            if (!property)
            {
                throw new Error("Cannot find property for '" + type + "." + key + "'");
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

DataList.prototype.resolveProperty = function (path, startIndex, type, typeParam)
{
    return this.followTypeDefinitionPath(path, startIndex, type, typeParam, true);
};

DataList.prototype.getPropertyModel = function(type, name)
{
    var domainType = this.types[type];
    var properties = domainType && domainType.properties;
    if (properties)
    {
        for (var i = 0; i < properties.length; i++)
        {
            var prop = properties[i];
            if (prop.name === name)
            {
                return prop;
            }
        }
    }
    return null;
};

module.exports = DataList;
