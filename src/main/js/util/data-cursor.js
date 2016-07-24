const immutableUpdate = require("react-addons-update");
const assign = require("object-assign");
const keys = require("./keys");
const util = require("./data-graph-util");

const DataGraphType = util.DataGraphType;
const WILDCARD_SYMBOL = util.WILDCARD_SYMBOL;

const converter = require("../service/property-converter");

/**
 * Encapsulates a pointer to a data graph structure and offers methods for access, modification and limited navigation.
 *
 * @param dataGraph     DataGraph instance
 * @param path          property path within the data graph
 * @param type          type of the cursor either domain type or property type
 * @param typeParam     type param if applicable for the type
 * @param isProperty    {boolean} true if this cursor points to a property
 * @param onChange      {boolean|function} normed cursor change handler
 * @constructor
 */
function DataCursor(dataGraph, path, type, typeParam, isProperty, onChange)
{
    util.validatePath(path);

    this.graph = dataGraph;
    this.rootObject = null;
    this.type = type;
    this.typeParam = typeParam;
    this.path = path;
    this.property = isProperty;
    this.onChange = onChange;

    this.value = this.get();

    // user data
    this.data = null;
}

DataCursor.prototype.isProperty = function ()
{
    return this.property;
};

DataCursor.prototype.get = function (path)
{
    var p = this.path;
    if (path && path.length)
    {
        p = p.concat(path);
    }
    return util.walk(this.graph.rootObject, p);
};

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

function createModificationMethod(op)
{
    return function (path, value)
    {
        path = path && path.length ? this.path.concat(path) : this.path;
        return this.update(spec(
            path,
            "$" + op,
            value
        ), path);
    }
}



/**
 * Adds the given array of values to end of the existing array at the target location.
 *
 * @param path      {Array} path relative to the cursor or null
 * @param values    {Array} array of values
 *
 * @returns {DataGraph} immutably updated data graph
 */
DataCursor.prototype.push = createModificationMethod("push");


/**
 * Adds the given array of values to beginning of the existing array at the target location.
 *
 * @param path      {Array} path relative to the cursor or null
 * @param values    {Array} array of values
 *
 * @returns {DataGraph} immutably updated data graph
 */
DataCursor.prototype.unshift = createModificationMethod("unshift");


/**
 * Executes a splice call on the target array for every element of the given
 * values
 *
 * @param path      {Array} path relative to the cursor or null
 * @param values    {Array} array of splice argument arrays
 *
 * @returns {DataGraph} immutably updated data graph
 */
DataCursor.prototype.splice = createModificationMethod("splice");


/**
 * Sets the target location to the given value.
 *
 * @param path      {Array} path relative to the cursor or null
 * @param value     {*} value
 *
 * @returns {DataGraph} immutably updated data graph
 */
DataCursor.prototype.set = createModificationMethod("set");


/**
 * Merges the object at the target location with the given object value.
 *
 * @param path      {Array} path relative to the cursor or null
 * @param value     {Object} object value
 * @returns {DataGraph} immutably updated data graph
 */
DataCursor.prototype.merge = createModificationMethod("merge");


/**
 * Applies the given function to the target location and replaces
 * the target location with the return value of that function.
 *
 * @param path      {Array} path relative to the cursor or null
 * @param value     {function} apply function
 * @returns {DataGraph} immutably updated data graph
 */
DataCursor.prototype.apply = createModificationMethod("apply");

/**
 * General purporse immutable update using the normal "react-addons-update" spec format.
 *
 * @param spec          Data spec
 * @param path          path information to use for this modification
 *
 * @returns {DataGraph} immutably updated data graph
 */
DataCursor.prototype.update = function (spec, path)
{
    let newGraph = this.graph.copy(
        immutableUpdate(
            this.graph.rootObject,
            spec)
    );

    let localChange = this.onChange;

    var propagate = localChange !== false;

    if (localChange && localChange !== true)
    {
        let result = localChange.call(this, newGraph, path);

        if (result && typeof result == "object" && typeof result.prototype.isRawDataGraph == "function")
        {
            newGraph = result;
        }

        if (result === false)
        {
            propagate = false;
        }
    }

    if (propagate)
    {
        this.graph.onChange.call(this.graph, newGraph, path);
    }

    this.graph = newGraph;
    this.value = this.get();

    return newGraph;
};

/**
 * Returns a new cursor whose path points to a parent of the current cursor
 *
 * @param howMany       {number} how many levels to pop. Default is 1
 *
 * @returns {DataCursor} new cursor
 */
DataCursor.prototype.pop = function (howMany)
{
    return this.graph.getCursor(this.path.slice(0, this.path.length - ( howMany || 1)));
};

/**
 * Returns a new cursor relative to the current one
 *
 * @param path          {Array} path added to the current cursor path or null
 * @param [onChange]    {boolean|function} cursor change handler
 */
DataCursor.prototype.getCursor = function (path, onChange)
{
    return this.graph.getCursor(path ? this.path.concat(path) : this.path, onChange);
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
DataCursor.prototype.getDomainObject = function (type)
{
    var object;

    var firstProperty = this.path[0];
    var dataGraph = this.graph;
    var value = dataGraph.rootObject[firstProperty];
    var property;

    if (this.path.length == 1)
    {
        // extract from data list root
        object = {};

        var cols = dataGraph.columns;
        if (!cols)
        {
            throw new Error("No cols");
        }
        var checkImplicit = false;

        if (this.graph.type === DataGraphType.OBJECT)
        {
            if (this.graph.isMap)
            {
                property = cols[WILDCARD_SYMBOL];
            }
            else
            {
                property = cols[firstProperty];
            }


            if (!property || property.type !== "DomainType")
            {
                throw new Error("Cannot extract domain type from property " + JSON.stringify(property));
            }

            return value;

        }

        for (var name in cols)
        {
            if (cols.hasOwnProperty(name))
            {
                property = cols[name];

                if (!type)
                {
                    type = property.domainType;
                    checkImplicit = true;
                }

                if (property.domainType === type)
                {
                    object[property.name] = value[name];
                }
                else if (checkImplicit)
                {
                    throw new Error("Implicit type detection failed: DataGraph contains '" + type + "' and '" + property.type + "' objects");
                }
            }
        }
        var typeDef = dataGraph.types[type];

        if (!typeDef)
        {
            throw new Error("No type definition for found for type " + JSON.stringify(type) );
        }

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

/**
 * Returns a property type descriptor.
 *
 * @param [path]    relative path to get the property descriptor from.
 *
 * @returns {{type: string, typeParam: string, parent: string}} property descriptor
 */
DataCursor.prototype.getPropertyType = function (path)
{
    var property;

    var type = this.type;
    var typeParam = this.typeParam;

    if (!path || !path.length)
    {
        type = util.ROOT_NAME;
        typeParam = null;
        path = this.graph.type === DataGraphType.ARRAY ? this.path.slice(1) : this.path;
    }

    if (type === util.ROOT_NAME)
    {
        var column = path[0];
        var columns = this.graph.columns;
        property = this.graph.isMap ? columns[WILDCARD_SYMBOL] : columns[column] ;

        if (!property)
        {
            throw new Error("No column '" + (this.graph.isMap ? WILDCARD_SYMBOL : column ) + "' in " + keys(columns))
        }

        if (path.length == 1)
        {
            return assign({
                parent: property.domainType
                //graph: this.graph
            }, property);
        }

        //console.log("property", property, key);
        type = property.type;
        typeParam = property.typeParam;
    }

    //console.log("getPropertyType start", type, typeParam)
    return util.followTypeDefinitionPath(this.graph, path, 1, type, typeParam, true);

};

/**
 * Return the value this cursor points to as value for this cursor.
 *
 */
DataCursor.prototype.valueOf = function ()
{
    return this.value;
};

/**
 * Creates a new cursor with the same path and change handler for the given new data graph.
 *
 * @param dataGraph      {DataGraph} data graph
 *
 * @returns {DataCursor} new cursor
 */
DataCursor.prototype.withNewGraph = function (dataGraph)
{
    return dataGraph.getCursor(this.path, this.onChange)
};



/**
 * Returns a string conversion of the value this cursor points to.
 *
 * @returns {string}
 */
DataCursor.prototype.toString = function ()
{
    return String(this.value);
};

/**
 * React link compatibility.
 *
 * @returns {string}
 */
DataCursor.prototype.requestChange = function (v)
{
    return this.set(null, v);
};

module.exports = DataCursor;
