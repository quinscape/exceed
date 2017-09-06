import update from "react-addons-update";
import keys from "../util/keys";
import isArray from "../util/is-array";
import walk from "../util/walk";
import assign from "object-assign";

import DataGraphType from "./graph-type";
import {DataGraph, WILDCARD_SYMBOL, ROOT_NAME, getColumnType, getPropertyModel, isMapGraph} from "./graph"

function Path(path)
{
    if (!path || typeof path !== "object" || typeof path.length !== "number")
    {
        throw new Error("Invalid path: " + JSON.stringify(path));
    }

    return path;
}

function followTypeDefinitionPath(types, path, startIndex, type, typeParam, resolve)
{
    let key, property = null, parent;

    for (let i = startIndex; i < path.length; i++)
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

            property = getPropertyModel(types, type, key);
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

function createSpec(path, op, value, root = {})
{
    let spec = root;

    for (let i = 0; i < path.length; i++)
    {
        spec = spec[path[i]] = {};
    }

    spec[op] = value;

    //console.log("SPEC", JSON.stringify(root));

    return root;
}

function executeUpdate(op, graph, cursor, value, path)
{
    const effectivePath = path && path.length ? cursor.path.concat(path) : cursor.path;

    const newGraph = assign({}, graph);

    const spec = createSpec(
        effectivePath,
        op,
        value
    );
    newGraph.rootObject = update(
        graph.rootObject,
        spec
    );

    cursor.updateGraph(newGraph);

    return newGraph;
}

/**
 * Creates a data cursor object for the given data graph and path.
 *
 * A data cursor is a complex js object that conceptually points at a editable location within a data graph.
 *
 * The cursor provides methods to read its values and immutably modify the underlying data graph to produce
 * new data graph versions.
 *
 * The cursor itself is not immutable as it contains the reference to an associated list.
 *
 * @param types     {object} domain types map
 * @param graph     {DataGraph} data graph object
 * @param path      {Array} path to walk from the root object within the graph
 * @returns {DataCursor} data cursor object
 */
class DataCursor {
    /**
     * @constructor
     */
    constructor(types, graph, path)
    {
        if (__DEV)
        {
            if (!types || typeof types !== "object")
            {
                throw new Error("Invalid domain types map: " + types);
            }

            graph = DataGraph(graph);
            path = Path(path);
        }

        if (graph.type === DataGraphType.ARRAY && path.length > 0 && typeof path[0] !== "number")
        {
            throw new Error("First key path entry must be a numeric row index");
        }

        let type = ROOT_NAME, typeParam = null, isProperty;

        if (graph.type === DataGraphType.OBJECT)
        {
            if (path.length)
            {
                let propertyDef = isMapGraph(graph) ? graph.columns[WILDCARD_SYMBOL] : graph.columns[path[0]];
                if (!propertyDef)
                {
                    throw new Error("No column " + path[0] + " in " + keys(graph.columns))
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

            if (graph.type === DataGraphType.OBJECT)
            {
                let column = path[0];

                let property = getColumnType(graph, column);
                type = property.type;
                typeParam = property.typeParam;

                if (path.length > 1)
                {
                    let typeInfo = followTypeDefinitionPath(types, path, 1, type, typeParam);
                    type = typeInfo.type;
                    typeParam = typeInfo.typeParam;
                }
            }
            else
            {
                let column = path[1];

                let property = getColumnType(graph, column);
                type = property.type;
                typeParam = property.typeParam;

                if (path.length > 2)
                {
                    let typeInfo = followTypeDefinitionPath(types, path, 2, type, typeParam);
                    type = typeInfo.type;
                    typeParam = typeInfo.typeParam;
                }
            }

            if (type === "DomainType")
            {
                type = typeParam;
                typeParam = null;
            }

            if (type === "Map" || type === "List" || types[type])
            {
                isProperty = false;
            }
        }

        this.graph = graph;
        this.domainTypes = types;
        this.type = type;
        this.typeParam = typeParam;
        this.path = path;
        this.property = isProperty;

        // XXX: deprecation traps. remove after a whilee
        Object.defineProperty(this, "value", {
            get: function ()
            {
                throw new Error("cursor.value is deprecated");
            }
        });

        this.requestChange = (newValue) =>
        {
            // XXX: deprecation trap
            throw new Error("cursor.requestChange is deprecated");
        };

    }


    isProperty()
    {
        return this.property;
    }

    /**
     * Returns a new cursor whose path points to a parent of the current cursor
     *
     * @param howMany   {number} how many levels to pop. Default is 1
     *
     * @returns {DataCursor} new cursor
     */
    pop(howMany = 1)
    {
        return new DataCursor(this.domainTypes, this.graph, this.path.slice(0, this.path.length - howMany));
    }

    /**
     * Extracts a domain object from the location the cursor currently points to. This can be either a domain object or
     * a data list root with mixed types.
     *
     * The method will fail if the cursor currently points to a property.
     *
     * @param type      {String?} type name, can be left out if the type is unambiguous
     * @returns {*}
     */
    getDomainObject(type)
    {
        let object;

        const firstProperty = this.path[0];
        const graph = this.graph;
        const value = graph.rootObject[firstProperty];
        let property;

        if (this.path.length === 1)
        {
            // extract from data list root
            object = {};

            let cols = graph.columns;
            if (!cols)
            {
                throw new Error("No cols");
            }
            let checkImplicit = false;

            if (graph.type === DataGraphType.OBJECT)
            {
                property = cols[WILDCARD_SYMBOL];
                if (!property)
                {
                    property = cols[firstProperty];
                }

                if (!property || property.type !== "DomainType")
                {
                    throw new Error("Cannot extract domain type from property " + JSON.stringify(property));
                }

                return value;

            }

            for (let name in cols)
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
            let typeDef = this.domainTypes[type];

            if (!typeDef)
            {
                throw new Error("No type definition for found for type " + JSON.stringify(type));
            }

            object._type = typeDef.name;
            return object;
        }
        else
        {
            const propType = this.getPropertyType(graph);
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
                    this.get(graph)
                );
            }
            else
            {
                object = assign({
                    _type: propType.type
                }, this.get(graph));
            }

            if (type && type !== object._type)
            {
                throw new Error("Type parameter requests '" + type + "' but cursor points to '" + object._type + "'");
            }

            return object;
        }
    }

    /**
     * Returns a property type descriptor.
     *
     * @param [path]    {Array?} relative path to get the property descriptor from.
     *
     * @returns {{type: string, typeParam: string, parent: string}} property descriptor
     */
    getPropertyType(path)
    {
        let property;

        let type = this.type;
        let typeParam = this.typeParam;

        const graph = this.graph;

        if (!path || !path.length)
        {
            type = ROOT_NAME;
            typeParam = null;
            path = graph.type === DataGraphType.ARRAY ? this.path.slice(1) : this.path;
        }

        if (type === ROOT_NAME)
        {
            const column = path[0];
            const columns = graph.columns;
            const isMap = isMapGraph(graph);
            property = isMap ? columns[WILDCARD_SYMBOL] : columns[column];

            if (!property)
            {
                throw new Error("No column '" + (isMap ? WILDCARD_SYMBOL : column ) + "' in " + keys(columns))
            }

            if (path.length === 1)
            {
                return assign({
                    parent: property.domainType
                    //graph: graph
                }, property);
            }

            //console.log("property", property, key);
            type = property.type;
            typeParam = property.typeParam;
        }

        //console.log("getPropertyType start", type, typeParam)
        return followTypeDefinitionPath(this.domainTypes, path, 1, type, typeParam, true);

    }

    /**
     * Appends one or more values to the array pointed at by the cursor.
     *
     * Makes the new list the current list of the cursor.
     *
     * @param value         {Array|*} value or array of values to append
     * @param path          {Array?} relative path to current cursor
     *
     * @returns {DataGraph} new data graph object
     */
    push(value, path)
    {

        if (!isArray(value))
        {
            value = [value];
        }

        return executeUpdate("$push", this.graph, this, value, path);
    }

    /**
     * Prepends one or more values to the array pointed at by the cursor.
     *
     * Makes the new list the current list of the cursor.
     *
     * @param value         {Array|*} value or array of values to prepend
     * @param path          {Array?} relative path to current cursor
     *
     * @returns {DataGraph} new data graph object
     */
    unshift(value, path)
    {

        if (!isArray(value))
        {
            value = [value];
        }

        return executeUpdate("$unshift", this.graph, this, value, path);
    }

    /**
     * Invokes a Array.splice operation for every array in a given array of arrays on the array pointed
     * to by the cursor.
     *
     * Makes the new list the current list of the cursor.
     *
     * @param spliceArrays  {Array} array of splice commands
     * @param path          {Array?} relative path to current cursor
     *
     * @returns {DataGraph} new data graph object
     */
    splice(spliceArrays, path)
    {
        return executeUpdate("$splice", this.graph, this, spliceArrays, path);
    }

    /**
     * Merges the object pointed at by the cursor with the given object
     *
     * Makes the new list the current list of the cursor.
     *
     * @param obj   {object} object to merge in
     * @param [path]  {Array} relative path to current cursor
     *
     * @returns {DataGraph} new data graph object
     */
    merge(obj, path)
    {
        return executeUpdate("$merge", this.graph, this, obj, path);
    }

    /**
     * Sets the given value at the cursor location.
     *
     * Makes the new list the current list of the cursor.
     *
     * @param value {*} value to set
     * @param [path]  {Array} relative path to current cursor
     * @returns {DataGraph} new data graph object
     */
    set(value, path)
    {
        return executeUpdate("$set", this.graph, this, value, path);
    }

    /**
     * Applies the given function to the value pointed at by the cursor.
     *
     * Makes the new list the current list of the cursor.
     *
     * @param fn {Function} function to apply
     * @param [path]  {Array} relative path to current cursor
     * @returns {DataGraph} new data graph object
     */
    apply(fn, path)
    {
        return executeUpdate("$apply", this.graph, this, fn, path);
    }

    /**
     * Returns a new cursor based on the current cursor and an optional relative path.
     *
     * @param [path]  {Array} relative path to current cursor
     * @returns {DataCursor}
     */
    getCursor(path)
    {
        return new DataCursor(this.domainTypes, this.graph, this.path.concat(path))
    }

    /**
     * Reads the value from the cursor
     *
     * @param [path]  {Array} relative path to current cursor
     * @returns {*} value
     */
    get(path)
    {
        if (path && path.length)
        {
            const value = walk(this.graph.rootObject, this.path);
            return walk(value, path);
        }
        else
        {
            return walk(this.graph.rootObject, this.path);
        }
    }

    /**
     * Retuns the path for this cursor. Can be used to store a cursor and recreate it later.
     * @returns {*}
     */
    getPath()
    {
        return this.path;
    }

    /**
     * Updates the cached data graph for this cursor. The update is automatically executed for
     * every immutable graph change operation.
     *
     * @param graph     {DataGraph} data graph object
     */
    updateGraph(graph)
    {
        this.graph = graph;
    }

    /**
     * Get the graph structure currently associated with this cursor instance
     *
     * @returns {DataGraph} current data graph object
     */
    getGraph()
    {
        return this.graph;
    }

    toString()
    {
        return String(this.get());
    }


    valueOf()
    {
        return this.get();
    }
}

export default DataCursor;
