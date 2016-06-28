
var assign = require("object.assign").getPolyfill();

var components = {};

var React = require("react");

function registerMapRecursively(map)
{
    //console.debug("registerMapRecursively", map);
    for (var name in map)
    {
        if (map.hasOwnProperty(name))
        {
            var value = map[name];
            if (typeof value === "object")
            {
                if (name === "components")
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

function resolveWizardComponents(componentDef, bulkDir, componentName)
{
    var componentClass;
    var templates = componentDef.templates;
    if (templates)
    {
        for (var i = 0; i < templates.length; i++)
        {
            var template = templates[i];
            if (template.wizard)
            {
                componentClass = bulkDir[template.wizard];

                if (!componentClass)
                {
                    throw new Error("Wizard component '" + template.wizard + "' not found for component '" + componentName + "'")
                }
                template.wizardComponent = componentClass;
            }
        }
    }

    var propWizards = componentDef.propWizards;
    if (propWizards)
    {
        for (var propName in propWizards)
        {
            if (propWizards.hasOwnProperty(propName))
            {
                var entry = propWizards[propName];

                componentClass = bulkDir[entry.wizard];
                if (!componentClass)
                {
                    throw new Error("Wizard prop component '" + entry.wizard + "' not found for component '" + componentName + "'")
                }

                entry.wizardComponent = componentClass;
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
    register: function(bulkDir, def)
    {
        //console.log("register def", bulkDir.DataGrid, def);

        var i, subComponents = def.components;
        for (var componentName in subComponents)
        {
            if (subComponents.hasOwnProperty(componentName))
            {
                var componentDef = subComponents[componentName];

                var parts = componentName.split(".");

                var component = bulkDir;
                for (i = 0; i < parts.length; i++)
                {
                    component = component[parts[i]];
                    if (!component)
                    {
                        throw new Error("Cannot find module for declared component '" + componentName + "'");
                    }
                }

                componentDef.component  = component;

                resolveWizardComponents(componentDef, bulkDir, componentName);

                components[componentName] = componentDef;
            }
        }
    }
};

if (typeof window !== "undefined")
{
    window.ComponentService = ComponentService;
}

module.exports = ComponentService;
