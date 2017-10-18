import endsWith from "../util/endsWith";
import isES2015 from "../util/is-es2015";

const COMPONENTS_JSON_SUFFIX = "/components.json";
const JS_EXTENSION = ".js";

const components = {};

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
                let moduleExport = ctx(moduleName);
                if (!moduleExport)
                {
                    throw new Error("Wizard component '" + wizardComponentName + "' not found for component '" + componentName + "'")
                }
                template.wizardComponent = isES2015(moduleExport) ? moduleExport.default: moduleExport;
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
                let moduleExport = ctx(moduleName);

                if (!moduleExport)
                {
                    throw new Error("Wizard prop component '" + entry.wizard + "' not found for component '" + componentName + "'")
                }

                entry.wizardComponent = isES2015(moduleExport) ? moduleExport.default: moduleExport;
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
            const componentDef = subComponents[componentName];

            const parts = componentName.split(".");
            const len = parts.length;
            const moduleExport = ctx(dir + parts[0] + JS_EXTENSION);
            let component;
            if (len === 1)
            {
                component = isES2015(moduleExport) ? moduleExport.default : moduleExport;
            }
            else
            {
                if (len[1] === "default")
                {
                    throw new Error("Invalid component name '" + componentName + "'");
                }
                component = moduleExport;
                for (let i = 1; i < len; i++)
                {
                    let part = parts[i];
                    if (part === "")
                    {
                        throw new Error("Invalid component name '" + componentName + "': contains empty property");
                    }
                    component = component[part];
                }
            }

            componentDef.component  = component;
            resolveWizardComponents(componentDef, ctx, dir, componentName);

            components[componentName] = componentDef;

            //console.log(componentName, {componentDef})
        }
    }
}


const ComponentService = {
    getComponents: function ()
    {
        //console.log("COMPONENTS", components);
        return components;
    },

    registerFromRequireContext: function (ctx)
    {
        let files = ctx.keys();

        //console.log("REGISTER", files);

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


module.exports = ComponentService;
