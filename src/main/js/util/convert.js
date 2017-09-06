import { validateDataGraph, isMapGraph, isArrayGraph, getColumnType } from "../domain/graph"
import keys from "./keys"
import isArray from "./is-array"
import assign from "object-assign"

let _domainService;
let _propertyConverter;

function getPropertyConverter()
{
    if (!_propertyConverter)
    {
        _propertyConverter = require("../service/property-converter").default;
    }
    return _propertyConverter;
}

function getDomainService()
{
    if (!_domainService)
    {
        _domainService = require("../service/domain");
    }
    return _domainService;
}

function convertMap(obj, propertyType)
{
    const converted = {};
    for (let name in obj)
    {
        if (obj.hasOwnProperty(name))
        {
            converted[name] = convertProperty(obj[name], propertyType);
        }
    }
    return converted;
}

function convertList(list, propertyType)
{
    const len = list.length;
    const converted = new Array(len);
    for (let i=0; i < len; i++)
    {
        converted[i] = convertProperty(list[i], propertyType);
    }
    return converted;
}

function convertDomainType(value, domainTypeName)
{
    const domainService = getDomainService();

    const nameFromValue = value._type;

    if (domainTypeName && domainTypeName !== nameFromValue)
    {
        throw new Error("Type mismatch: Object contains '" + nameFromValue + "' while converting '" + domainTypeName + "'");
    }

    const domainType = domainService.getDomainType(nameFromValue);
    if (!domainType)
    {
        throw new Error("Unknown domain type '" + nameFromValue + "'");
    }

    const converted = { _type: nameFromValue };
    const { properties } = domainType;
    for (let i = 0; i < properties.length; i++)
    {
        const pt = properties[i];

        const { name } = pt;
        converted[name] = convertProperty(value[name], pt);
    }
    return converted;
}

function convertProperty(value, propertyType)
{

    const typeParam = propertyType.typeParam;

    let result;

    if (value === null)
    {
        result = null;
    }
    else
    {
        switch(propertyType.type)
        {
            case "List":
                result = convertList(value, getPropertyTypeForComplex(typeParam, propertyType.config));
                break;
            case "Map":
                result = convertMap(value, getPropertyTypeForComplex(typeParam, propertyType.config));
                break;
            case "DomainType":
                result = convertDomainType(value, typeParam);
                break;
            default:
                result = getPropertyConverter().fromServer(value, propertyType);
                break;
        }
    }
    //console.loglog("CONVERT PROPERTY", value, " => ", result, { propertyType });

    return result;
}

function convertRoot(graph, rootObj)
{
    const names = keys(graph.columns);

    const converted = {};
    for (let i = 0; i < names.length; i++)
    {
        const name = names[i];

        const pt = getColumnType(graph, name);

        converted[name] = convertProperty(rootObj[name], pt);
    }
    return converted;
}

function getPropertyTypeForComplex(typeParam, config)
{
    const domainService = getDomainService();
    if (domainService.getDomainType(typeParam))
    {
        return {
            type: "DomainType",
            typeParam: typeParam,
            config: null
        };
    }
    else
    {
        return {
            type: typeParam,
            typeParam: null,
            config: config
        };
    }
}

/**
 * Converts the given data-graph containing structure from server format to client format.
 *
 * The object given can be either a datagraph or a complex object graph with embedded data-graphs.
 *
 * @param graph     datagraph structure
 */
export function convert(graph)
{
    //console.loglog("CONVERT", graph);
    if (typeof graph !== "object")
    {
        return graph;
    }
    else if (validateDataGraph(graph))
    {
        //console.log("converting graph", graph);

        const copy = assign({}, graph);

        if (isMapGraph(graph))
        {
            //console.loglog("Convert map graph", graph);
            copy.rootObject = convertMap(graph.rootObject, getColumnType(graph));
        }
        else if (isArrayGraph(graph))
        {
            //console.loglog("Convert array graph", graph);
            const array = graph.rootObject;
            const len = array.length;

            const converted  = new Array(len);
            for (let i = 0; i < len; i++)
            {
                converted[i] = convertRoot(graph, array[i]);
            }
            copy.rootObject = converted;
        }
        else
        {
            //console.loglog("Convert object graph", graph);
            copy.rootObject = convertRoot(graph, graph.rootObject);
        }
        return copy;
    }
    else
    {
        if (isArray(graph))
        {
            const length = graph.length;
            const converted = new Array(length);

            for (let i = 0; i < length; i++)
            {
                converted[i] = convert(graph[i]);
            }

            return converted;
        }
        else
        {
            const converted = {};
            for (let name in graph)
            {
                if (graph.hasOwnProperty(name))
                {
                    converted[name] = convert(graph[name]);
                }
            }
            return converted
        }
    }
}

export function convertComponentData(data)
{
    const componentData = {};
    for (let name in data)
    {
        if (data.hasOwnProperty(name))
        {
            componentData[name] = convert(data[name]);
        }
    }
    return componentData;
}

export function convertComponents(componentUpdate)
{
    const component = {};

    for (let name in componentUpdate)
    {
        if (componentUpdate.hasOwnProperty(name))
        {
            component[name] = {
                data: convertComponentData(componentUpdate[name].data),
                vars: componentUpdate[name].vars
            };
        }
    }

    return component;
}
