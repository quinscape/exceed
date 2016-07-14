/**
 *  Renders Javasript source code from the JSON view model.
 *
 *  It creates the described component and context hierarchy and creates the react class for that view.
 *
 *  If we're in production mode and NO_CATCH_ERRORS is not set, we instrument the generated view-source by hand
 *  to have catch-errors protection for the view component, too.
 */

var catchErrors;
var ErrorReport;

var sys = require("../sys");

// we repeat this full expression so that uglify is clever enough to really strip the code when inactive
if (process.env.NODE_ENV !== "production" && process.env.NO_CATCH_ERRORS !== "true")
{
    catchErrors = require("react-transform-catch-errors");
    ErrorReport = require("../ui/ErrorReport.es5");
}

var componentService = require("./component");

var React = require("react");
var components = componentService.getComponents();

var rtViewAPI = require("./runtime-view-api");
var rtActionAPI = require("./runtime-action-api");

var ComponentClasses = require("../components/component-classes");

const RENDERED_IF_PROP = "renderedIf";

function RenderContext(out)
{
    this.usedComponents = {};
    this.out = out;
    this.contexts = [];
}

function indent(ctx, depth)
{
    for (var i = 0; i < depth; i++)
    {
        ctx.out.push("  ");
    }
}

var trivialExpr = /^(true|false|null|\d+|\w+)$/;


function hasClass(componentDescriptor, cls)
{
    return componentDescriptor && componentDescriptor.classes && componentDescriptor.classes.indexOf(cls) >= 0;
}

function evaluateExpression(componentModel,attrName)
{
    var exprs = componentModel.exprs;
    var attrs = componentModel.attrs;
    if (exprs && exprs.hasOwnProperty(attrName))
    {
        var expr = exprs[attrName];

        var attr = attrs && attrs[attrName];
        if (!trivialExpr.test(expr) && attr)
        {
            expr +=  " /* " + attr + " */"
        }

        return expr;
    }
    return attrs && JSON.stringify(attrs[attrName]);
}

function renderRecursively(ctx, componentModel, depth, childIndex)
{
    var name = componentModel.name;
    var value, component, i;
    var attrName;

    var componentDescriptor;
    var isComponent = components.hasOwnProperty(name);
    if (isComponent)
    {
        if (name === "ViewContext")
        {
            ctx.out.push("false");
            return;
        }

        componentDescriptor = components[name];
        component = name;


        // record usage of component module export
        ctx.usedComponents[name.split(".")[0]] = true;
    }
    else
    {
        if (name === "[String]")
        {
            indent(ctx, depth);
            ctx.out.push(evaluateExpression(componentModel, "value"));
            return;
        }

        component = JSON.stringify(name);
        //console.log("builtin", component, components);
    }

    var attrs = componentModel.attrs;
    var exprs = componentModel.exprs;
    var kids = componentModel.kids;

    if (componentDescriptor)
    {
        var queries = componentDescriptor.queries;
        var propTypes = componentDescriptor.propTypes;

        var contextKey = attrs && componentDescriptor.contextKey;
        var currentContextName = ctx.contexts[0] || "undefined";
    }

    indent(ctx, depth);

    var attrsStartIndex;

    if (exprs && exprs[RENDERED_IF_PROP])
    {
        ctx.out.push("!!(" + evaluateExpression(componentModel, RENDERED_IF_PROP) + ") && ");
    }


    ctx.out.push("_React.createElement(", component, ", ");

    var hasInjection = !!queries || !!componentDescriptor && componentDescriptor.dataProvider;

    if (hasInjection)
    {
        ctx.out.push("_v.inject({\n")
    }
    else
    {
        ctx.out.push("{\n");
        attrsStartIndex = ctx.out.length;
    }

    var first = true;

    var commaIfNotFirst = function ()
    {
        if (!first)
        {
            ctx.out.push(",\n");
        }
        first = false;
    };

    if (attrs)
    {
        for (attrName in attrs)
        {
            if (attrs.hasOwnProperty(attrName) && ( !exprs || !exprs.hasOwnProperty(attrName)))
            {
                value = attrs[attrName];
                commaIfNotFirst();
                indent(ctx, depth + 2);
                if (propTypes && propTypes[attrName] && (attrName === "var" || propTypes[attrName].client === false))
                {
                    ctx.out.push("//");
                }
                ctx.out.push(JSON.stringify(attrName), " : ", JSON.stringify(attrs[attrName]));
            }
        }
    }

    if (exprs)
    {
        for (attrName in exprs)
        {
            if (exprs.hasOwnProperty(attrName) && attrName !== RENDERED_IF_PROP)
            {
                commaIfNotFirst();
                indent(ctx, depth + 2);
                if (propTypes && propTypes[attrName] && propTypes[attrName].client === false)
                {
                    ctx.out.push("//");
                }
                ctx.out.push(JSON.stringify(attrName), " : ", evaluateExpression(componentModel, attrName));
            }
        }
    }

    //console.log ("DESCRIPTOR", componentDescriptor, ComponentClasses.MODEL_AWARE);
    if (hasClass(componentDescriptor, ComponentClasses.MODEL_AWARE))
    {
        commaIfNotFirst();
        indent(ctx, depth + 2);
        ctx.out.push(" \"viewModel\": _v.viewModel");
    }


    if (childIndex !== undefined)
    {
        commaIfNotFirst();
        indent(ctx, depth + 2);
        ctx.out.push("key: ", childIndex);
    }

    if (componentDescriptor)
    {
        if (componentDescriptor.context || contextKey)
        {
            commaIfNotFirst();
            indent(ctx, depth + 2);
            ctx.out.push("context: ", currentContextName);
        }

        if (queries)
        {
            commaIfNotFirst();
            indent(ctx, depth + 2);
            ctx.out.push("vars: _v.data[\"" + attrs.id + "\"].vars");
        }

        if (kids && componentDescriptor.providesContext)
        {
            var newContextName = (attrs && attrs.var) || "context";
            ctx.contexts.unshift(newContextName);

            commaIfNotFirst();
            indent(ctx, depth + 2);
            ctx.out.push("renderChildren", " : function(" + newContextName + "){\n");
            indent(ctx, depth + 3);
            ctx.out.push("return ([\n");
            for (i = 0; i < kids.length; i++)
            {
                if (i > 0)
                {
                    ctx.out.push(",\n");
                }
                renderRecursively(ctx, kids[i], depth + 4, i);
            }

            ctx.out.push("\n");
            indent(ctx, depth + 2);
            ctx.out.push("])}");

            ctx.contexts.shift();
        }
    }

    if (hasInjection)
    {
        ctx.out.push("\n");
        indent(ctx, depth + 1);
        ctx.out.push("}, _v.data[\"" + attrs.id + "\"].data)");
    }
    else
    {
        // if we're still right were we were before we started generating non-injected attributes
        if (attrsStartIndex == ctx.out.length)
        {
            // we just rewrite the last entry to give React a null props
            ctx.out[attrsStartIndex - 1] = "null";
        }
        else
        {
            ctx.out.push("\n");
            indent(ctx, depth + 1);
            ctx.out.push("}");
        }
    }

    if (kids && (!componentDescriptor || !componentDescriptor.providesContext))
    {
        for (i = 0; i < kids.length; i++)
        {
            ctx.out.push(",\n");
            renderRecursively(ctx, kids[i], depth + 1);
        }
    }
    ctx.out.push("\n");
    indent(ctx, depth);
    ctx.out.push(")");
}

function renderViewComponentSource(viewModel)
{
    var buf = [];

    buf.push(
        "var _v= new _RTView(this.props.model, this.props.componentData);\n",
        "return (\n\n"
    );

    var ctx = new RenderContext(buf);

    renderRecursively(ctx, viewModel.root, 1);

    var usedComponents = ctx.usedComponents;
    for (var componentName in usedComponents)
    {
        if (usedComponents.hasOwnProperty(componentName))
        {
            buf.unshift("var " + componentName + " = _components[\"" + componentName + "\"].component;\n");
        }
    }

    buf.push(
        "\n);"
    );

    return buf.join("");
}

module.exports = {
    createRenderFunction : function(viewModel)
    {
        //console.log("\nVIEWMODEL:\n", JSON.stringify(viewModel, null, "  "));
        var code = renderViewComponentSource(viewModel);
        //console.log("\nRENDER-FN:\n", code);

        var renderFn = new Function("_React", "_components", "_RTView", "_catchErrors", "_ErrorReport", "_sys", "_a", code);
        return {
            src: code,
            fn: function (component)
            {
                return renderFn.call(component, React, components, rtViewAPI, catchErrors, ErrorReport, sys, rtActionAPI);
            }
        };
    }
};
