const DataCursor = require("./data-cursor");

const util = require("./data-graph-util");
const keys = require("./keys");

const DataGraphType = util.DataGraphType;
const WILDCARD_SYMBOL = util.WILDCARD_SYMBOL;



var count = 0;

/**
 * Encapsulates a data-graph structure and provides immutable update function on it.
 *
 * @param types                 map of known domain types
 * @param dataGraph             {object|DataGraph} raw data graph JSON or DataGraph instance
 * @param dataGraph.type        {DataGraphType} type of this data. Either DataGraphType.ARRAY or DataGraphType.OBJECT.
 * @param dataGraph.columns     {object} map of column property descriptors. For OBJECT graphs, there can be a single entry
 *                              "*" to describe the type of all object values (a map like object).
 * @param dataGraph.rootObject  {array|object} data payload corresponding to the type.
 * @param dataGraph.count       count of existing row for paged LIST graphs, number of entries for OBJECT graphs.
 * @param [onChange]            change callback
 * @constructor
 */
function DataGraph(types, dataGraph, onChange)
{
    //this.id = ++count;

    this.type = dataGraph.type;
    this.columns = dataGraph.columns;
    this.rootObject = dataGraph.rootObject;
    this.count = dataGraph.count;

    if (!DataGraphType.isValid(this.type))
    {
        throw new TypeError("Invalid DataGraphType: " + this.type);
    }

    if (dataGraph instanceof DataGraph)
    {
        this.types = dataGraph.types;
        this.onChange = onChange || dataGraph.onChange;
        this.isMap = dataGraph.isMap;
    }
    else
    {
        this.types = types;
        this.onChange = onChange;
        this.isMap = validateWildcard(dataGraph.columns);
    }
}


/**
 * Returns a shallow raw data graph JSON object copy for the current DataGraph instance.
 *
 * @returns {{type: *, columns: *, rootObject: *, count: *}}
 */
DataGraph.prototype.getRaw = function ()
{
    return {
        type : this.type,
        columns : this.columns,
        rootObject: this.rootObject,
        count: this.count
    };
};


DataGraph.prototype.getColumnType = function (column)
{
    var property;
    if (this.type === DataGraphType.ARRAY)
    {
        property = this.columns[column];
    }
    else
    {
        property = this.isMap ? this.columns[WILDCARD_SYMBOL] : this.columns[column];
    }

    if (!property)
    {
        throw new Error("No column '" + column + "' in DataGraph columns ( " + keys(this.columns) + " )");
    }
    return property;

};

/**
 * Creates a new DataCursor for the given path with the given optional change handler.
 *
 * If the given change handler argument is true (the default), the global change handler is called for changes.
 *
 * If the given change handler is a function, this function will be used as cursor change handler.
 *
 * If the given change handler argument is false, no change handlers are called, the cursor is local.
 *
 *
 * Cursor Change Handlers
 * ----------------------
 *
 * If the cursor is provided with a change handler function, that change handler will be called with in cases of changes
 * happening over this cursor. The method can inspect the changes and do one of three things
 *
 *     a) Return false to prevent that change from happening / stop propagation.
 *     b) Amend the changes that happened in the received data graph and return a new copy with additional changes which
 *        will cause both changes to happen at once / in one update.
 *     c) Return nothing / undefined to let the change happen as it was.
 *
 *
 * Local Cursors
 * -------------
 *
 * Giving false als change handler causes no change handler to be called at all. This is useful to have produce local
 * copies of a data graph (e.g. in another cursor's change handler)
 *
 *
 * @param path              path array.
 * @param [onChange]        {boolean|function} cursor change handler.
 *
 * @returns {DataCursor}
 */
DataGraph.prototype.getCursor = function (path, onChange)
{
    util.validatePath(path);

    onChange = validateLocalChangeHandler(onChange);


    if (this.type === DataGraphType.ARRAY && typeof path[0] !== "number")
    {
        throw new Error("First key path entry must be a numeric row index");
    }

    var type = util.ROOT_NAME, typeParam = null, isProperty;


    if (this.type === DataGraphType.OBJECT)
    {
        if (path.length)
        {
            let propertyDef = this.isMap ? this.columns[WILDCARD_SYMBOL] : this.columns[path[0]];
            if (!propertyDef)
            {
                throw new Error("No column " + path[0] + " in " + keys(this.columns))
            }

            isProperty = (
                propertyDef.type !== "DomainType" &&
                propertyDef.type !== "Map" &&
                propertyDef.type !== "List"
            );
            type = propertyDef.type;
            typeParam = propertyDef.typeParam;
        }
    }
    else
    {
        isProperty = false;
    }

    if (path.length > 1)
    {
        isProperty = true;

        if (this.type === DataGraphType.OBJECT)
        {
            let column = path[0];

            let property = this.getColumnType(column);
            type = property.type;
            typeParam = property.typeParam;

            if (path.length > 1)
            {
                let typeInfo = util.followTypeDefinitionPath(this, path, 1, type, typeParam);
                type = typeInfo.type;
                typeParam = typeInfo.typeParam;
            }
        }
        else
        {
            let column = path[1];

            let property = this.getColumnType(column);
            type = property.type;
            typeParam = property.typeParam;

            if (path.length > 2)
            {
                let typeInfo = util.followTypeDefinitionPath(this, path, 2, type, typeParam);
                type = typeInfo.type;
                typeParam = typeInfo.typeParam;
            }
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

    return new DataCursor(this, path, type, typeParam, isProperty, onChange);
};

DataGraph.prototype.getPropertyModel = function(type, name)
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


/**
 * Returns a new DataGraph with updated root object
 *
 * @param newRoot   {?Array} new root
 * @returns {DataGraph}
 */
DataGraph.prototype.copy = function (newRoot)
{
    var copy = new DataGraph(null, this);
    if (newRoot !== undefined)
    {
        copy.rootObject = newRoot;
    }
    return copy;
};

DataGraph.prototype.isRawDataGraph  = function (input)
{
    return input && DataGraphType.isValid(input.type) >= 0 && input.rootObject && input.columns;
};

function validateWildcard(columns)
{
    var currentIsStar;
    var isStar;

    if (!columns || typeof columns != "object")
    {
        throw new Error("Invalid colums map:", columns);
    }

    for (var name in columns)
    {
        if (columns.hasOwnProperty(name))
        {
            currentIsStar = (name === WILDCARD_SYMBOL);
            if (isStar === undefined)
            {
                isStar = currentIsStar;
            }
            else
            {
                if (isStar || currentIsStar)
                {
                    throw new TypeError("'*' must be the only column definition for maps");
                }
                break;
            }
        }
    }

    if (isStar === undefined)
    {
        throw new TypeError("Columns can't be empty.");
    }

    return isStar;
}

function validateLocalChangeHandler(onChange)
{
    if (onChange === undefined)
    {
        return true;
    }

    var type = typeof onChange;
    if (type != "boolean" && type != "function")
    {
        throw new Error("Invalid cursor change handler: " + onChange);
    }

    return onChange;
}

if (process.env.NODE_ENV !== "production")
{
    const EXPOSE_TO_TESTS = require("./interceptor")();
    EXPOSE_TO_TESTS.validateLocalChangeHandler = validateLocalChangeHandler;
    EXPOSE_TO_TESTS.validateWildcard = validateWildcard;
}

module.exports = DataGraph;
