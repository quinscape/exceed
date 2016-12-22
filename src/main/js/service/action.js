const Promise = require("es6-promise-polyfill").Promise;

const ajax = require("./ajax");
const uri = require("../util/uri");
const sys = require("../sys");

const  MODULE_NAME_REGEXP = /^.\/(.*)\.js/;

let actions;

function defaultCatch(e)
{
    return Promise.reject(e);
}

const ActionService = {
    registerFromRequireContext: function (ctx)
    {
        const modules = ctx.keys();
        for (let i = 0; i < modules.length; i++)
        {
            const moduleName = modules[i];
            const module = ctx(moduleName);

            if (typeof module !== "function")
            {
                throw new Error("Action module '" + moduleName + "' does not export a function");
            }

            const actionName = moduleName.replace(MODULE_NAME_REGEXP, "$1");
            ActionService.register(actionName, module, module.catch);
        }
    },

    register: function (actionName, handler, errorHandler)
    {
        //console.log("Register", actionName, handler, errorHandler);

        actions[actionName] = {
            client: true,
            server: false,
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
                action.server = true;
                //throw new Error("There are a client and a server function registered under the name '" + name + "'");
            }
            else
            {
                actions[name] = {
                    server: true,
                    client: false
                };
            }
        }
    },
    getActions: function ()
    {
        return actions;
    },

    /**
     * Executes the action as specified by the given action model.
     *
     * @param action        {object|function} Action model, must contain an identifying "action" property containing the name of either a
     *                      client or a server action. Can also be just a function that will be executed and that can return a promise.
     * @param [forceServer] {boolean} if set to true the action will not be executed as client execution even if such a client action
     *                      should exist. This is useful for server actions with a client side decoration. The client side decoration needs
     *                      to set forceServer to true to prevent an endless loop.
     * @returns {*}
     */
    execute: function (action, forceServer)
    {
        if (!action)
        {
            return Promise.reject(new Error("No action"));
        }

        if (typeof action === "function")
        {
            try
            {
                var result = action();
                return Promise.resolve(result);
            }
            catch (e)
            {
                return Promise.reject(e);
            }
        }
        else if (typeof action === "object" && typeof action.action === "string")
        {
            var entry = actions[action.action];

            if (!entry)
            {
                return Promise.reject(new Error("No action registered for name '" + action.action + "'"))
            }

            if (entry.client && !forceServer)
            {
                try
                {
                    return Promise.resolve( entry.handler(action));
                }
                catch(e)
                {
                    var catchResult = entry.catch(e, action);

                    if (!catchResult || !catchResult.then)
                    {
                        return Promise.reject(new Error("Catch function for " + action.action + " did not return a Promise value, original error:" + e));
                    }
                    return catchResult;
                }
            }

            return ajax({
                url: uri( "/action/{app}/{action}", {
                    app: sys.appName,
                    action: action.action
                }),
                method: "POST",
                contentType: "application/json",
                data: action
            });
        }
        else
        {
            return  Promise.reject(new Error("Invalid action" + action));
        }
    },

    reset: function ()
    {
        actions = {};
    }
};

module.exports = ActionService;
