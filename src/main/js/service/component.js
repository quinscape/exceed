
var components = {};


function registerMapRecursively(root, map, path)
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
                    if (typeof value.component === "string")
                    {
                        register(root, path, value);
                    }
                    else if (value.length && typeof value[0] === "string")
                    {
                        for (var i = 0; i < value.length; i++)
                        {
                            register(root, path, value[i]);
                        }
                    }
                }
                else
                {
                    registerMapRecursively(root, value, path.concat(name));
                }
            }
        }
    }
}

function register(componentsMap, path, def)
{
    var component = componentsMap;

    for (var i = 0; i < path.length; i++)
    {
        var name = path[i];
        component = component[name];
    }
    ComponentService.register(def, component);
}



var ComponentService = {
    getComponents: function ()
    {
        return components;
    },

    registerBulk: function (bulkMap)
    {
        registerMapRecursively(bulkMap, bulkMap, []);
    },
    register: function(def, component)
    {
        console.log("register", def, component);
    }
};

window.ComponentService = ComponentService;

module.exports = ComponentService;
