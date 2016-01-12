
var extend = require("extend");

var components = {};

var React = require("react");

function registerMapRecursively(map)
{
    //console.debug("registerMapRecursively", componentsMap, path);
    for (var name in map)
    {
        if (map.hasOwnProperty(name))
        {
            var value = map[name];
            if (typeof value === "object")
            {
                if (name === "component")
                {
                    if (value.components && typeof value.components === "object")
                    {
                        ComponentService.register(map, value);
                    }
                }
                else
                {
                    registerMapRecursively(map[name], value);
                }
            }
        }
    }
}

var ComponentService = {
    getComponents: function ()
    {
        return components;
    },

    registerBulk: function (bulkMap)
    {
        registerMapRecursively(bulkMap);
    },
    register: function(dir, def)
    {
        //console.log("register def", dir.DataGrid, def);

        var subComponents = def.components;
        for (var name in subComponents)
        {
            if (subComponents.hasOwnProperty(name))
            {
                var componentDef = subComponents[name];

                var parts = name.split(".");

                var component = dir;
                for (var i = 0; i < parts.length; i++)
                {
                    component = component[parts[i]];
                    if (!component)
                    {
                        throw new Error("Cannot find module for declared component '" + name + "'");
                    }
                }

                components[name] = extend({
                    component : component
                }, componentDef);
            }
        }
    }
};

if (typeof window !== "undefined")
{
    window.ComponentService = ComponentService;
}

module.exports = ComponentService;
