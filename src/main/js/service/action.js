var ajax = require("./ajax");
var uri = require("../util/uri");

var enableThrobber = require("../cando").ajax;

var Throbber = require("../ui/throbber");

var actions = {};

function defaultCatch(e)
{
    return Promise.reject(e);
}

function register(map)
{
    //console.log("register(%o, %s)", map, path);
    for (var name in map)
    {

        if (map.hasOwnProperty(name))
        {
            var action = map[name];
            if (typeof action === "function")
            {
                ActionService.register(name, action, action.catch);
            }
            else
            {
                register(action);
            }
        }
    }
}

function executeAction(actionModel, data)
{
    //console.log("EXECUTE ACTION", actionModel, data);

    var entry = actions[actionModel.action];
    if (entry.client)
    {
        try
        {
            return Promise.resolve( entry.handler(actionModel, data));
        }
        catch(e)
        {
            var catchResult = entry.catch(e, actionModel, data);

            if (!catchResult || !catchResult.then)
            {
                return Promise.reject(new Error("Catch function for " + actionModel.action + " did not return a Promise value, original error:" + e));
            }
            return catchResult;
        }
    }

    return ajax({
        url: uri("/action/{app}/{action}", {
                app: appName,
                action: actionModel.action,
                model: JSON.stringify(actionModel)
            }),
        method: "POST",
        contentType: "application/json",
        data: data
    });
}

function dispatchNext(array, index)
{
    return function(data)
    {
        return executeInternal(array[index], data);
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

function executeInternal(action,data)
{
    var promise;

    if (typeof action === "function")
    {
        try
        {
            var result = action(data);
            promise = Promise.resolve(result);
        }
        catch(e)
        {
            promise = Promise.reject(e);
        }

        return promise.catch(function (e)
        {
            console.error("Error executing\n", action, "\nwith data ", data, e);
            return Promise.reject(e);
        })
    }
    else if (typeof action === "object")
    {
        if (action.length && action[0])
        {
            return pipe(action, data);
        }
        else
        {
            return executeAction(action, data)
                .catch(function (e)
                {
                    console.error("Error executing\n", action, "\nwith data ", data, e);
                    return Promise.reject(e);
                });
        }
    }
    else
    {
        return  Promise.reject(new Error("Invalid action" + action));
    }
}


function throbberDisableAndReject(e)
{
    Throbber.disable();
    return Promise.reject(e);
}

var ActionService = {
    registerBulk: register,

    register: function (actionName, handler, errorHandler)
    {
        //console.log("Register", actionName, handler, errorHandler);
        actions[actionName] = {
            client: true,
            handler: handler,
            catch: errorHandler || defaultCatch
        };
    },

    /**
     *
     */
    initServerActions: function (actionNames)
    {
        for (var i = 0; i < actionNames.length; i++)
        {
            var name = actionNames[i];
            var action = actions[name];
            if (action)
            {
                throw new Error("There are a client and a server function registered under the name '" + name + "'");
            }

            actions[name] = {
                client: false
            };
        }
    },
    getActions: function ()
    {
        return actions;
    },
    clearActions: function ()
    {
        actions = {};
    },
    execute: function (action, data)
    {
        if (!action)
        {
            return Promise.reject(new Error("No action"));
        }

        enableThrobber && Throbber.enable();

        var promise = executeInternal(action, data);

        if (enableThrobber)
        {
            return promise.then(Throbber.disable, throbberDisableAndReject)
        }
        else
        {
            return promise;
        }
    }

};

module.exports = ActionService;
