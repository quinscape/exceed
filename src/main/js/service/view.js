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

window.debug = function (a)
{
    console.log("DEBUG", a);
    return a;
};

function renderRecursively(buf, componentModel, depth, usedComponents)
{
    var name = componentModel.name;
    var code;
    var value;
    var component;

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

    indent(buf, depth);



    var attrs = componentModel.attrs;
    var queries = componentDescriptor && componentDescriptor.queries;

    buf.push("React.createElement(", component, ", ");

    if (queries)
    {
        buf.push("extend({")
    }
    else
    {
        buf.push("{")
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
                    buf.push(", ");
                }
                first = false;

                var last = (value.length - 1);
                if (typeof value === "string" && value.length > 2 && value[0] === '{' && value[last] === "}")
                {
                    code = value.substr(1, last - 1);
                }
                else
                {
                    code = JSON.stringify(value);
                }

                buf.push(JSON.stringify(attrName), " : ", code);
            }
        }
    }

    if (queries)
    {
        buf.push("}, this.props.componentData[\"" + attrs.id + "\"])\n");
    }
    else
    {
        buf.push("}\n");
    }

    var kids = componentModel.kids;
    if (kids)
    {
        buf.push(",");
        for (var i = 0; i < kids.length; i++)
        {
            var kidModel = kids[i];
            renderRecursively(buf, kidModel, depth + 1, usedComponents);
        }
    }

    indent(buf, depth);
    buf.push(")\n");
}

/**
 * Handles converting the view JSON models into js code
 * @type {{}}
 */
var ViewService = {

    renderView: function (name, model)
    {
        var buf = [
            "(function(React, components, extend){ return React.createClass({ displayName: \"" + name +"\", render: function () {\n\n"];

        var usedComponents = {};

        var headerPos = buf.length;

        buf.push("\n    return (\n\n")

        renderRecursively(buf, model.root, 2, usedComponents);

        for (var name in usedComponents)
        {
            if (usedComponents.hasOwnProperty(name))
            {
                buf.splice(headerPos, 0, "    var " + name + " = components[\"" + name + "\"].component;\n");
            }
        }

        buf.push("\n    );}});\n})");
        return buf.join("");
    },

    getViewComponent: function (name, model)
    {
        var component = viewComponents[name];
        if (!component)
        {
            var code = ViewService.renderView(name, model);

            console.debug(code)

            component = eval(code)(React, components, extend);

            viewComponents[name] = component;
        }

        return component;
    }
};
module.exports = ViewService;
