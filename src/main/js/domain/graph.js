import DataGraphType from "./graph-type"
import keys from "../util/keys"
import isArray from "../util/is-array"

export const ROOT_NAME = "[DataGraphRoot]";

export const WILDCARD_SYMBOL = "*";

/**
 * This module contains helper functions for data graph object structures which follow the description below.
 *
 * The object graph is stored as simple js object and can be cloned and modified without special tools.
 *
 * The data cursor implementation in "./cursor.js" offers data access, navigation and immutable updates for
 * these data graph structures.
 *
 * The structure corresponds to de.quinscape.exceed.runtime.component.DataGraph on the server side.
 *
 * @name DataGraph
 * @type {{
 *     type: DataGraphType,
 *     columns: Object,
 *     rootObject: Object|Array,
 *     count: Number,
 *     isMap: bool
 * }}
 */

/**
 * Validates a columns definition in terms of containing the wildcard.
 *
 * If the map contains the wildcard, it must be the only member.
 *
 * @param columns
 * @returns {*}
 */
export function validateWildcard(columns)
{
    let currentIsStar;
    let isStar = null;

    if (!columns || typeof columns !== "object")
    {
        throw new Error("Invalid colums map:", columns);
    }

    for (let name in columns)
    {
        if (columns.hasOwnProperty(name))
        {
            currentIsStar = (name === WILDCARD_SYMBOL);
            if (isStar === null)
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

    if (isStar === null)
    {
        throw new TypeError("Columns can't be empty.");
    }

    return isStar;
}

function error(msg, checkOnly)
{
    if (checkOnly)
    {
        return false;
    }
    throw new TypeError(msg);
}

/**
 * Validates a data graph object and returns it.
 *
 * @param graph         {object} object to check for being a data graph
 * @param _checkOnly     {boolean?} don't throw but return true if valid
 * @returns {DataGraph|boolean} validated graph or boolean, if we're just checking
 */
export function DataGraph(graph, _checkOnly = false)
{
    if (this instanceof DataGraph )
    {
        throw new Error("DataGraph musn't be called as constructor. Just use graph = Datagraph(value).");
    }

    const { type, rootObject, count } = graph;

    if (!DataGraphType.isValid(type))
    {
        return error("Invalid graph type: " + type, _checkOnly);
    }

    if (type === DataGraphType.OBJECT && typeof rootObject !== "object")
    {
        return error("Invalid root object for OBJECT graph" + rootObject, _checkOnly);

    }
    else  if (type === DataGraphType.ARRAY && !isArray(rootObject))
    {
        return error("Invalid root object for ARRAY graph" + rootObject, _checkOnly);
    }

    validateWildcard(graph.columns);

    if (typeof count !== "number")
    {
        return error("Invalid graph count: " + count, _checkOnly);
    }

    return _checkOnly || graph
}

export function isMapGraph(graph)
{
    return (graph.type === DataGraphType.OBJECT && !!graph.columns[WILDCARD_SYMBOL])
}

export function getColumnType(graph, column)
{
    if (__DEV)
    {
        graph = DataGraph(graph)
    }

    let property;
    if (graph.type === DataGraphType.ARRAY)
    {
        property = graph.columns[column];
    }
    else
    {
        property =  isMapGraph(graph) ? graph.columns[WILDCARD_SYMBOL] : graph.columns[column];
    }

    if (!property)
    {
        throw new Error("No column '" + column + "' in DataGraph columns ( " + keys(graph.columns) + " )");
    }
    return property;

}

export function getPropertyModel(types, type, name)
{
    const domainType = types[type];
    const properties = domainType && domainType.properties;
    if (properties)
    {
        for (let i = 0; i < properties.length; i++)
        {
            const prop = properties[i];
            if (prop.name === name)
            {
                return prop;
            }
        }
    }
    return null;
}

export function validateDataGraph(object)
{
    return DataGraph(object, true);
}

export default DataGraph;
