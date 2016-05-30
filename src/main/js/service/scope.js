var viewService;

function getViewService()
{
    if (!viewService)
    {
        viewService = require("../service/view");
    }
    return viewService;
}

function createScopeFn(type)
{
    return function(name)
    {
        var scope = getViewService().getRuntimeInfo().scope;
        console.log("SCOPE", scope);

        var value = scope[name];
        if (value === undefined)
        {
            console.error("Scoped " + type + " '" + name + "' is undefined")
        }
        return value;
    };
}

/**
 * Provides access to the scoped values available in the current context.
 *
 * This module is one of the target for static call detection via babel-plugin-track-usage
 *
 * @type {{list: Scope.list, object: Scope.object, property: Scope.property}}
 */
var Scope = {
    list: createScopeFn("list"),
    object: createScopeFn("object"),
    property: createScopeFn("property"),
    objectType: function (name)
    {
        return getViewService().getRuntimeInfo().scopeTypes[name];
    }
};

module.exports = Scope;
