/**
 *  Renders JavaScript source code from the JSON view model.
 *
 *  It creates the described component and context hierarchy and creates the react class for that view.
 *
 *  If we're in production mode and NO_CATCH_ERRORS is not set, we instrument the generated view-source by hand
 *  to have catch-errors protection for the view component, too.
 */

let catchErrors;
let ErrorReport;

import sys from "../sys";

// we repeat this full expression so that uglify is clever enough to really strip the code when inactive
// if (__DEV && !process.env.NO_CATCH_ERRORS)
// {
//     catchErrors = require("react-transform-catch-errors");
//     ErrorReport = require("../ui/ErrorReport.es5");
// }

const React = require("react");

import rtViewAPI from "./runtime-view-api"
import rtActionAPI from "./runtime-action-api"
import ViewWrapper from "../ui/ViewWrapper";
import i18n from "./i18n";
import ComponentClasses from "../components/component-classes"

import ComponentError from "../ui/ComponentError"

const RENDERED_IF_PROP = "renderedIf";

import * as Reducers from "../reducers";

function RenderContext(out, components, contentMap)
{
    this.out = out;
    this.components = components;
    this.contentMap = contentMap;

    //this.usedComponents = {};
    this.contexts = [];
}

function indent(ctx, depth)
{
    for (let i = 0; i < depth; i++)
    {
        ctx.out.push("  ");
    }
}

const trivialExpr = /^(true|false|null|\d+|\w+)$/;


function hasClass(componentDescriptor, cls)
{
    return componentDescriptor && componentDescriptor.classes && componentDescriptor.classes.indexOf(cls) >= 0;
}

function evaluateExpression(componentModel,attrName)
{
    const exprs = componentModel.exprs;
    const attrs = componentModel.attrs;
    if (exprs && exprs.hasOwnProperty(attrName))
    {
        const expr = exprs[attrName];
        const attr = attrs && attrs[attrName];
        if (!trivialExpr.test(expr) && attr)
        {
            return expr + " /* " + attr + " */"
        }
        return expr;
    }
    return attrs && JSON.stringify(attrs[attrName]);
}

function renderRecursively(ctx, componentModel, depth, childIndex)
{
    const name = componentModel.name;


    const isComponent = ctx.components.hasOwnProperty(name);

    let componentSource;
    let componentDescriptor;

    if (isComponent)
    {

        componentDescriptor = ctx.components[name];

        if (hasClass(componentDescriptor, ComponentClasses.CONFIGURATION))
        {
            ctx.out.push("false");
            return;
        }

        componentSource = "_c['" + name + "'].component";


        // record usage of component module export
        //ctx.usedComponents[name.split(".")[0]] = true;
    }
    else
    {
        if (name === "[String]")
        {
            indent(ctx, depth);
            ctx.out.push(evaluateExpression(componentModel, "value"));
            return;
        }

        componentSource = JSON.stringify(name);
    }

    //console.log("COMPONENT", componentSource);

    const attrs = componentModel.attrs;
    const exprs = componentModel.exprs;
    const kids = componentModel.kids;

    let queries, propTypes, contextKey, currentContextName;

    if (componentDescriptor)
    {
        queries = componentDescriptor.queries;
        propTypes = componentDescriptor.propTypes;

        contextKey = attrs && componentDescriptor.contextKey;
        currentContextName = ctx.contexts[0] || "undefined";
    }


    if (name === "Content")
    {
        const contentName = (attrs && attrs.name) || "main";
        const contentRoot = ctx.contentMap[contentName];
        if (!contentRoot)
        {
            throw new Error("No content '" + contentName + "' found");
        }
        renderRecursively(ctx, contentRoot, depth, childIndex);
        return;
    }


    indent(ctx, depth);

    let attrsStartIndex;

    if (exprs && exprs[RENDERED_IF_PROP])
    {
        ctx.out.push("!!(" + evaluateExpression(componentModel, RENDERED_IF_PROP) + ") && ");
    }

    const hasInjection = !!queries || !!componentDescriptor && componentDescriptor.dataProvider;

    if (hasInjection)
    {
        ctx.out.push("_React.createElement(_ComponentError, { componentId: " + JSON.stringify(attrs.id) + "},");
        depth++;
    }

    ctx.out.push("_React.createElement(", componentSource, ", ");


    if (hasInjection)
    {
        ctx.out.push("_v.inject({\n")
    }
    else
    {
        ctx.out.push("{\n");
        attrsStartIndex = ctx.out.length;
    }

    let first = true;

    const commaIfNotFirst = function ()
    {
        if (!first)
        {
            ctx.out.push(",\n");
        }
        first = false;
    };

    if (attrs)
    {
        for (let attrName in attrs)
        {
            if (attrs.hasOwnProperty(attrName) && ( !exprs || !exprs.hasOwnProperty(attrName)))
            {
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
        for (let attrName in exprs)
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

        if (componentDescriptor && componentDescriptor.vars)
        {
            commaIfNotFirst();
            indent(ctx, depth + 2);
            ctx.out.push("vars: _r.getComponentVars(_state, \"" + attrs.id + "\")");
        }
    }

    if (hasInjection)
    {
        ctx.out.push("\n");
        indent(ctx, depth + 1);
        ctx.out.push("}, _r.getComponentInjections(_state, \"" + attrs.id + "\"))");
    }
    else
    {
        // if we're still right were we were before we started generating non-injected attributes
        if (attrsStartIndex === ctx.out.length)
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

    if (kids)
    {
        if (!componentDescriptor || !componentDescriptor.providesContext)
        {
            for (let i = 0; i < kids.length; i++)
            {
                ctx.out.push(",\n");
                renderRecursively(ctx, kids[i], depth + 1);
            }
        }
        else
        {

            ctx.out.push(",\n");

            const newContextName = (attrs && attrs.var) || "context";
            ctx.contexts.unshift(newContextName);
            indent(ctx, depth + 1);
            ctx.out.push("function(" + newContextName + "){\n");
            indent(ctx, depth + 2);
            ctx.out.push("return ([\n");
            for (let i = 0; i < kids.length; i++)
            {
                if (i > 0)
                {
                    ctx.out.push(",\n");
                }
                renderRecursively(ctx, kids[i], depth + 4, i);
            }
            ctx.out.push("\n");
            indent(ctx, depth + 1);
            ctx.out.push("])}");

            ctx.contexts.shift();
        }
    }

    if (hasInjection)
    {
        ctx.out.push("\n");
        indent(ctx, depth);
        ctx.out.push(")");
        depth--;
    }
    
    ctx.out.push("\n");
    indent(ctx, depth);
    ctx.out.push(")");
}

function renderViewComponentSource(viewModel, components)
{
    //console.log("renderViewComponentSource", viewModel);

    const buf = [];

    buf.push(
        "var _store = this.props.store;\n",
        "var _state = _store.getState();\n",
        "var _v= new _RTView(_store);\n",
//        "console.log('REDUCERS', _r);\n",
        "return (\n\n",
        "  _React.createElement( _ViewWrapper, { title: ", viewModel.titleExpr || "\"\"" , ", store: _store},\n"
    );

    const contentMap = viewModel.content;
    const ctx = new RenderContext(buf, components, contentMap);
    renderRecursively(ctx, contentMap.root, 2);

    //buf.unshift("console.log('COMPONENTS', _c);\n");
    // const usedComponents = ctx.usedComponents;
    // for (let componentName in usedComponents)
    // {
    //     if (usedComponents.hasOwnProperty(componentName))
    //     {
    //         buf.unshift("var " + componentName + " = _c[\"" + componentName + "\"].component;\n");
    //     }
    // }

    buf.push(
        "\n  )\n);"
    );

    return buf.join("");
}

export default {
    createRenderFunction : function(viewModel, components)
    {
        const code = renderViewComponentSource(viewModel, components);
        //console.log("\nRENDER-FN:\n", code);

        const renderFn = new Function("_React", "_c", "_RTView", "_ComponentError", "_sys", "_a", "i18n", "_ViewWrapper", "_r", code);
        return {
            src: code,
            fn: function (component)
            {
                return renderFn.call(component, React, components, rtViewAPI, ComponentError, sys, rtActionAPI, i18n, ViewWrapper, Reducers);
            }
        };
    }
};
