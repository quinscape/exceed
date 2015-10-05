"use strict";

var ajax = require("./ajax");
var uri = require("../util/uri");

var Promise = require("es6-promise").Promise;

var clientActions = {

};


function dispatchNext(array, index)
{
    return function(data)
    {
        return dispatcher.dispatch(array[index], data);
    };
}

function pipe(array, data)
{
    var index = 0;
    var promise = dispatchNext(array, index++)(data);
    while (index < array.length)
    {
        promise = promise.then(dispatchNext(array, index++));
    }
    return promise;
}

function join(path,name)
{
    return path ? path + "/" + name : name;
}

function registerBulkRecursive(requireMap, path)
{
    for (var name in requireMap)
    {
        if (requireMap.hasOwnProperty(name))
        {
            var value = requireMap[name];
            if (typeof value === "function")
            {
                var actionName = join(path, name);
                dispatcher.registerAction(actionName, value);
            }
            if (value && typeof value === "object")
            {
                registerBulkRecursive(value, join(path, name));
            }
        }
    }
}

function executeAction(action, data)
{
    //console.log("EXECUTE ACTION", action);

    var clientAction = clientActions[action.action];
    if (clientAction)
    {
        return Promise.resolve(clientAction(data, action));
    }

    var actionName = action.action;
    return ajax({
        url: uri("/action/{name}", { name: actionName }),
        method: "POST",
        data: {
            params: JSON.stringify(action),
            data: data ? JSON.stringify(data) : "null"
        }
    });
}

var dispatcher = {
    /**
     * Dispatches the given action and given data and returns a promise that resolves with the action result after
     * all actions involved are done.
     *
     * @param action        {string|{action:string}|function|array} action.
     * @param data          data payload
     *
     * @returns {Promise} promise
     */
    dispatch: function(action, data)
    {
        if (!action)
        {
            return Promise.reject(new Error("No action"));
        }

        if (typeof action === "string")
        {
            action = {
                action: action
            };
        }

        if (typeof action === "function")
        {
            return Promise.resolve(action(data));
        }
        else if (typeof action === "object")
        {

            if (action.length && action[0])
            {
                return pipe(action, data);
            }
            else
            {
                return executeAction(action, data);
            }
        }
    },
    /**
     * Registers a client action implementation
     *
     * @param name  {string} action name
     * @param fn    {function} action implementation function(data,opts) returning potentially the new data-
     *              The implementations can return both sync values or promises.
     *
     */
    registerAction: function (name, fn)
    {
        //console.log("REGISTER ACTION", name);

        if (typeof fn !== "function")
        {
            throw new Error("fn is no function:", fn);
        }
        clientActions[name] = fn;
    },
    /**
     * Register all custom actions in the given require map.
     *
     * @param requireMap    bulk-require/bulkify style require map.
     */
    registerBulk: function (requireMap)
    {
        registerBulkRecursive(requireMap, "");
    },
    isClientAction: function(name)
    {
        return clientActions.hasOwnProperty(name);
    }
};
module.exports = dispatcher;
