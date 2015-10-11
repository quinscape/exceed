var componentService = require("./component");

var React = require("react/addons");
var extend = require("extend");

var components = componentService.getComponents();

var viewComponents = {

};

function indent(buf, depth)
{
    for (var i = 0; i < depth; i++)
    {
        buf.push("    ");
    }
}

var generatedIdRegEx = /^id-[a-z0-5]+$/;

var expressionRegEx = /^\{\s*(.*?)\s*}$/;

window.debug = function (a)
{
    console.log("DEBUG", a);
    return a;
};

function renderRecursively(buf, componentModel, depth, usedComponents)
{
    var name = componentModel.name;
    var code, value, component, m;

    var componentDescriptor;
    var isComponent = components.hasOwnProperty(name);
    if (isComponent)
    {
        componentDescriptor = components[name];
        component = name;

        usedComponents[name.split(".")[0]] = true;
    }
    else
    {
        component = JSON.stringify(name);
    }

    var attrs = componentModel.attrs;
    var queries = componentDescriptor && componentDescriptor.queries;

    indent(buf, depth);
    buf.push("React.createElement(", component, ", ");

    if (queries)
    {
        buf.push("extend({\n")
    }
    else
    {
        buf.push("{\n")
    }

    var first = true;
    for (var attrName in attrs)
    {
        if (attrs.hasOwnProperty(attrName))
        {
            value = attrs[attrName];

            if (isComponent || attrName !== "id" || !generatedIdRegEx.test(value))
            {

                if (!first)
                {
                    buf.push(",\n");
                }
                first = false;

                var last = (value.length - 1);
                if (typeof value === "string" && (m = expressionRegEx.exec(value)))
                {
                    code = m[1];
                }
                else
                {
                    code = JSON.stringify(value);
                }

                indent(buf, depth + 2);
                buf.push(JSON.stringify(attrName), " : ", code);
            }
        }
    }

    if (queries)
    {
        buf.push("\n");
        indent(buf, depth + 1);
        buf.push("}, this.props.componentData[\"" + attrs.id + "\"])");
    }
    else
    {
        buf.push("\n");
        indent(buf, depth + 1);
        buf.push("}");
    }

    var kids = componentModel.kids;
    if (kids)
    {
        for (var i = 0; i < kids.length; i++)
        {
            buf.push(",\n");
            var kidModel = kids[i];
            renderRecursively(buf, kidModel, depth + 1, usedComponents);
        }
    }
    buf.push("\n");
    indent(buf, depth);
    buf.push(")");
}

/**
 * Handles converting the view JSON models into js code and caching the React component results.
 *
 * @type {{}}
 */
var ViewService = {

    renderView: function (name, model)
    {
        var buf = [ "\nreturn React.createClass({\n    displayName: \"" + name +"\",\n\n    render: function ()\n    {\n        return (\n\n"];

        var usedComponents = {};

        renderRecursively(buf, model.root, 3, usedComponents);

        for (var componentName in usedComponents)
        {
            if (usedComponents.hasOwnProperty(componentName))
            {
                buf.splice(0, 0, "var " + componentName + " = components[\"" + componentName + "\"].component;\n");
            }
        }

        buf.push("\n        );\n    }\n});");
        return buf.join("");
    },

    getViewComponent: function (name, model, regenerate)
    {
        var component = viewComponents[name];
        if (!component || regenerate)
        {
            var code = ViewService.renderView(name, model);

            console.debug(code);

            component = new Function("React", "components", "extend", code)(React, components, extend);

            viewComponents[name] = component;
        }

        return component;
    }
};
module.exports = ViewService;
