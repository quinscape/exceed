const endsWith = require("../util/endsWith");

const COMPONENTS_JSON_SUFFIX = "/components.json";
const JS_EXTENSION = ".js";

var components = {};

function resolveWizardComponents(componentDef, ctx, dir, componentName)
{
    const templates = componentDef.templates;
    if (templates)
    {
        for (let i = 0; i < templates.length; i++)
        {
            let template = templates[i];
            let wizardComponentName = template.wizard;
            if (wizardComponentName)
            {
                let moduleName = dir + wizardComponentName + JS_EXTENSION;
                let componentClass = ctx(moduleName);
                if (!componentClass)
                {
                    throw new Error("Wizard component '" + wizardComponentName + "' not found for component '" + componentName + "'")
                }
                template.wizardComponent = componentClass;
            }
        }
    }

    const propWizards = componentDef.propWizards;
    if (propWizards)
    {
        for (let propName in propWizards)
        {
            if (propWizards.hasOwnProperty(propName))
            {
                let entry = propWizards[propName];
                let moduleName = dir + entry.wizard + JS_EXTENSION;
                let componentClass = ctx(moduleName);

                if (!componentClass)
                {
                    throw new Error("Wizard prop component '" + entry.wizard + "' not found for component '" + componentName + "'")
                }

                entry.wizardComponent = componentClass;
            }
        }
    }
}

function register(ctx, name)
{
    let dir = name.substr(0, name.length - COMPONENTS_JSON_SUFFIX.length + 1);

    //console.log("register", name, dir);

    let def = ctx(name);

    let subComponents = def.components;
    for (let componentName in subComponents)
    {
        if (subComponents.hasOwnProperty(componentName))
        {
            let componentDef = subComponents[componentName];

            let parts = componentName.split(".");
            let component = ctx(dir + parts[0] + JS_EXTENSION);

            for (let i = 1; i < parts.length; i++)
            {
                let part = parts[i];
                if (part === "")
                {
                    throw new Error("Invalid component name '" + componentName + "': contains empty property");
                }
                component = component[part];
            }
            componentDef.component  = component;
            resolveWizardComponents(componentDef, ctx, dir, componentName);

            components[componentName] = componentDef;
        }
    }
}


var ComponentService = {
    getComponents: function ()
    {
        return components;
    },

    registerFromRequireContext: function (ctx)
    {
        let files = ctx.keys();

        //console.log("KEYS", files);

        for (let i = 0; i < files.length; i++)
        {
            let name = files[i];

            if (endsWith(name, COMPONENTS_JSON_SUFFIX))
            {
                register(ctx, name);
            }
        }
    }
};

if (typeof window !== "undefined")
{
    window.ComponentService = ComponentService;
}

module.exports = ComponentService;
