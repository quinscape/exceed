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
        var scope = getViewService().getScope();
        //console.log("SCOPE", scope);

        var value = scope.get([name]);
        if (value === undefined)
        {
            console.error("Scoped " + type + " '" + name + "' is undefined")
        }
        return value;
    };
}

function createScopeCursorFn(type)
{
    return function(name)
    {
        var scope = getViewService().getScope();

        return scope.getCursor([name]);
//        console.log("SCOPE CURSOR", scope.id, name, cursor.value);
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

    listCursor: createScopeCursorFn("list"),

    objectCursor: createScopeCursorFn("object"),

    propertyCursor: createScopeCursorFn("property"),

    objectType: function (name)
    {
        return getViewService().getRuntimeInfo().scopeInfo.objectTypes[name];
    },

    propertyType: function (name)
    {
        return getViewService().getRuntimeInfo().scopeInfo.propertyTypes[name];
    },

    getScopeUpdate: function (names)
    {

        var viewService = getViewService();

        // if we're not asked for specific keys, we use all view context keys
        names = names || viewService.getViewContextKeys();

        //
        names = names.concat(viewService.getDirtyScopeKeys());

        var values = {};

        var scope = viewService.getScope().get();

        for (var i = 0; i < names.length; i++)
        {
            var name = names[i];
            values[name] = scope[name];
        }
        return values;
    }
};

module.exports = Scope;
