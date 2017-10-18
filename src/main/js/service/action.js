const Promise = require("es6-promise-polyfill").Promise;

import ajax from "./ajax"
import uri from "../util/uri";
import isES2015 from "../util/is-es2015";
import sys from "../sys";

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
            const moduleExport = ctx(moduleName);
            const actionFn = isES2015(moduleExport) ? moduleExport.default : moduleExport;

            if (typeof actionFn !== "function")
            {
                throw new Error("Action module '" + moduleName + "' does not export a function");
            }

            const actionName = moduleName.replace(MODULE_NAME_REGEXP, "$1");
            ActionService.register(actionName, actionFn, actionFn.catch);
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
     * @param actionName    Name of the action to call
     * @param args          {Array} arguments for the action
     *
     * @returns {*}
     */
    execute: function (actionName, args)
    {
        if (!actionName)
        {
            throw new Error("No action name");
        }

        const entry = actions[actionName];

        if (!entry)
        {
            return Promise.reject(new Error("No action registered for name '" + actionName + "'"))
        }

        if (entry.client)
        {
            try
            {
                return Promise.resolve(
                    entry.handler.apply(null, args)
                );
            }
            catch(e)
            {
                const catchArgs = [e].concat(args);
                const catchResult = entry.catch.apply(null, catchArgs) ;

                if (!catchResult || !catchResult.then)
                {
                    return Promise.reject(new Error("Catch function for " + actionName + " did not return a Promise value, original error:" + e));
                }
                return catchResult;
            }
        }

        return ActionService.serverAction(actionName, args);
    },

    serverAction: function (actionName, args)
    {
        return ajax({
            url: uri( "/action/{app}/{action}", {
                app: sys.appName,
                action: actionName,
            }),
            method: "POST",
            contentType: "application/json",
            data: { args }
        }).then(function (actionResult) {
            
        });
    },

    reset: function ()
    {
        actions = {};
    }
};

ActionService.reset();

module.exports = ActionService;
