import update from "immutability-helper";
import keys from "../util/keys";
import isArray from "../util/is-array";
import walk from "../util/walk";
import assign from "object-assign";

import { validateDataGraph, objectToDataGraph } from "../domain/graph"

import arrayEquals from "../util/array-equals";
import describeProperty from "../util/describe-property";
import DataGraphType from "./graph-type";
import {
    DataGraph,
    getColumnType,
    getPropertyModel,
    isArrayGraph,
    isMapGraph,
    ROOT_NAME,
    WILDCARD_SYMBOL
} from "./graph"

const domainService = require("../service/domain");

function Path(path)
{
    if (!path || typeof path !== "object" || typeof path.length !== "number")
    {
        throw new Error("Invalid path: " + JSON.stringify(path));
    }

    return path;
}

function followTypeDefinitionPath(domainData, path, startIndex, type, typeParam, resolve)
{
    let key;

    const state = {
        property: null,
        parent: null,
        type,
        typeParam
    };

    //console.log("followTypeDefinitionPath", state);

    for (let i = startIndex; i < path.length; i++)
    {
        key = path[i];

        //console.log("key = ", key);

        if (state.type === "List")
        {
            if (typeof key !== "number")
            {
                if (key === "length")
                {
                    state.type = "Integer";
                    state.typeParam = null;
                    state.property = null;
                }
                else
                {
                    throw new Error("List indices must be numeric:" + path)
                }
            }

            state.parent = state.type;
            resolveCollectionType(domainData, state);
            state.property = null;
        }
        else if (state.type === "Map")
        {
            state.parent = state.type;
            resolveCollectionType(domainData, state);
            state.property = null;
        }
        else if (state.type === "DomainType")
        {
            state.parent = state.typeParam;
            resolveCollectionType(domainData, state);
            state.property = getPropertyModel(domainData, state.typeParam, key);
            if (!state.property)
            {
                throw new Error("Cannot find property for '" + state.typeParam + ":" + key + "'");
            }
            state.type = state.property.type;
            state.typeParam = state.property.typeParam;
        }
        else
        {
            throw new Error("Cannot walk " +  key + " from " + describeProperty(state) );
        }

        //console.log("CURRENT", state.type, state.typeParam);
    }

    if (resolve)
    {
        if (!state.property)
        {
            return {
                parent: state.parent,
                type: state.type,
                typeParam: state.typeParam,
            };
        }
        else
        {
            return assign({
                parent: state.parent
            }, state.property);
        }
    }
    else
    {
        return {
            type: state.type,
            typeParam: state.typeParam
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
 * @param domainData    {object} app domain data
 * @param graph         {DataGraph} data graph object
 * @param path          {Array} path to walk from the root object within the graph
 * @returns {DataCursor} data cursor object
 */
class DataCursor {

    static from(data, path = [])
    {
        if (!data)
        {
            throw new Error("No data");
        }

        if (validateDataGraph(data))
        {
            return new DataCursor(domainService.getDomainData(), data, path);
        }
        else if (data instanceof DataCursor)
        {
            return data;
        }
        else if (data._type)
        {
            return new DataCursor(
                domainService.getDomainData(),
                objectToDataGraph(domainService, data),
                [0]
            );
        }
        else
        {
            console.error("Cannot handle data", data);
        }
    }

    /**
     * @constructor
     */
    constructor(domainData, graph, path)
    {
        if (__DEV)
        {
            if (!domainData || typeof domainData !== "object")
            {
                throw new Error("Invalid domain types map: " + domainData);
            }

            graph = DataGraph(graph);
            path = Path(path);
        }

        if (graph.type === DataGraphType.ARRAY && path.length > 0 && typeof path[0] !== "number")
        {
            throw new Error("First key path entry must be a numeric row index: " + JSON.stringify(path));
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
                    let typeInfo = followTypeDefinitionPath(domainData, path, 1, type, typeParam);
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
                    let typeInfo = followTypeDefinitionPath(domainData, path, 2, type, typeParam);
                    type = typeInfo.type;
                    typeParam = typeInfo.typeParam;
                }
            }

            // if (type === "DomainType")
            // {
            //     type = typeParam;
            //     typeParam = null;
            // }

            if (type === "Map" || type === "List"  || type === "Enum"  || type === "StateMachine" || type === "DomainType")
            {
                isProperty = false;
            }
        }

        this.graph = graph;
        this.domainData = domainData;
        this.type = type;
        this.typeParam = typeParam;
        this.path = path;
        this.property = isProperty;
    }


    getType()
    {
        return this.type;
    }

    getTypeParam()
    {
        return this.typeParam;
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
        return new DataCursor(this.domainData, this.graph, this.path.slice(0, this.path.length - howMany));
    }

    /**
     * Extracts the object identities present at the given cursor position (which is popped once if it is pointed to a property).
     *
     * It reconstructs the object identifies from the columns contained in the query or from objects embedded in it.
     *
     * @returns {object} object mapping query names to objects
     */
    extractObjects()
    {
        const graph = this.graph;
        const columns = graph.columns;

        // queryName -> domain object
        const objects = {};
        // object with preliminary objects pending id check
        const preliminary = {};

        const index = this.path[0];

        for (let name in columns)
        {
            if (columns.hasOwnProperty(name))
            {
                const column = columns[name];
                if (column.config && column.config.queryName)
                {
                    const queryName = column.config.queryName;

                    const value = graph.rootObject[index][name];
                    if (typeof value === "undefined")
                    {
                        throw new Error("Undefined column value [" + index + "][" + name + "] in " + JSON.stringify(this.graph));
                    }

                    if (column.type === "DomainType")
                    {
                        objects[queryName] = value;
                    }
                    else
                    {
                        let obj = preliminary[queryName];
                        if (!obj)
                        {
                            obj = domainService.create(column.domainType, null);
                            preliminary[queryName] = obj;
                        }

                        obj[column.name] = value
                    }
                }
            }
        }


        // only take those preliminary object that have an id.
        for (let name in preliminary)
        {
            if (preliminary.hasOwnProperty(name))
            {
                const obj = preliminary[name];
                objects[name] = obj;
            }
        }

        return objects;
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

        return followTypeDefinitionPath(this.domainData, path, 1, type, typeParam, true);

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
        return new DataCursor(this.domainData, this.graph, this.path.concat(path))
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

    /**
     * Returns true if the given cursor points to same graph and the same location.
     *
     * @param that      {DataCursor} other cursor
     * @returns {boolean} true if the cursors point to the same location in the same graph
     */
    equals(that)
    {
        if (this === that)
        {
            return true;
        }

        if (this.graph !== that.graph)
        {
            return false;
        }

        return arrayEquals(this.path, that.path);
    }

    valueOf()
    {
        return this.get();
    }
}

/**
 * Resolves the collection type referenced by the given property model and mutably updates that propertyModel
 * with the collection type.
 *
 *
 *
 * @param domainData
 * @param propertyModel
 */
export function resolveCollectionType(domainData, /*mutable*/ propertyModel)
{
    const typeParam = propertyModel.typeParam;
    if (domainData.domainTypes[typeParam])
    {
        propertyModel.type = "DomainType";
        propertyModel.typeParam = typeParam;
    }
    else if (domainData.enumTypes[typeParam])
    {
        propertyModel.type = "Enum";
        propertyModel.typeParam = typeParam;
    }
    else if (domainData.stateMachines[typeParam])
    {
        propertyModel.type = "StateMachine";
        propertyModel.typeParam = typeParam;
    }
    else
    {
        propertyModel.type = typeParam;
        propertyModel.typeParam = null;
    }
}


export default DataCursor;
