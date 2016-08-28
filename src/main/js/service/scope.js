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
    return ;
}

/**
 * Provides access to the scoped values available in the current context.
 *
 * This module is one of the target for static call detection via babel-plugin-track-usage
 *
 * @type {{list: Scope.list, object: Scope.object, property: Scope.property}}
 */
var Scope = {

    property: function(name)
    {
        var scope = getViewService().getScope();
        //console.log("SCOPE", scope);

        var value = scope.getCursor([name]).value;
        if (value === undefined)
        {
            console.error("Scoped value '" + name + "' is undefined")
        }
        return value;
    },

    propertyCursor: function(name)
    {
        var scope = getViewService().getScope();
        var cursor = scope.getCursor([name]);

//        console.log("SCOPE CURSOR", scope.id, name, cursor.value);

        return cursor;
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

        var scope = viewService.getScope().rootObject;

        for (var i = 0; i < names.length; i++)
        {
            var name = names[i];
            values[name] = scope[name];
        }
        return values;
    }
};

module.exports = Scope;
