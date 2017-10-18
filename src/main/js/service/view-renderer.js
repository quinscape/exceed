/**
 *  Renders JavaScript source code from the JSON view model.
 *
 *  It creates the described component and context hierarchy and creates the react class for that view.
 *
 *  If we're in production mode and NO_CATCH_ERRORS is not set, we instrument the generated view-source by hand
 *  to have catch-errors protection for the view component, too.
 */

import sys from "../sys";
import RTView from "./runtime-view-api"
import rtActionAPI from "./runtime-action-api"
import ViewWrapper from "../ui/ViewWrapper";
import i18n from "./i18n";
import ComponentClasses from "../components/component-classes"

import ComponentError from "../ui/ComponentError"
import * as Reducers from "../reducers";
import { DEFAULT_NAME } from "../editor/editor-defaults";

const React = require("react");

const RENDERED_IF_PROP = "renderedIf";
const MODEL_PROP = "model";
const VALUE_PROP = "value";
const DISABLED_IF = "disabledIf";
const READ_ONLY_IF = "readOnlyId";

function RenderContext(out, components, contentMap)
{
    this.out = out;
    this.components = components;
    this.contentMap = contentMap;
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

function evaluateExpression(componentModel,attrName, noComment)
{
    const exprs = componentModel.exprs;
    const attrs = componentModel.attrs;
    if (exprs && exprs.hasOwnProperty(attrName))
    {
        const expr = exprs[attrName];
        const attr = attrs && attrs[attrName];
        if (!trivialExpr.test(expr) && attr && !noComment)
        {
            return expr + " /* " + attr + " */"
        }
        return expr || "null";
    }

    const attrValue = attrs && JSON.stringify(attrs[attrName]);

    return attrValue || "null";
}

function isFormStateComponent(name, componentDescriptor)
{
    if (name === "FormBlock" || name === "Button" || name === "TButton")
    {
        return true;
    }
    return componentDescriptor && componentDescriptor.classes && componentDescriptor.classes.indexOf(ComponentClasses.FIELD) >= 0
}

function inIterativeContext(ctx, componentModel)
{
    let current = componentModel.parent;
    while (current)
    {
        const componentDescriptor = ctx.components[current.name];
        const isIterative = hasClass(componentDescriptor, ComponentClasses.ITERATIVE_CONTEXT);

        //console.log({componentModel, isIterative});

        if (isIterative)
        {
            return true;
        }

        current = current.parent;
    }

    return false;
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

    const queries = componentDescriptor && componentDescriptor.queries;
    const propTypes = componentDescriptor && componentDescriptor.propTypes;

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

    // if (hasInjection)
    // {
    //     ctx.out.push("_React.createElement(_ComponentError, { componentId: " + JSON.stringify(attrs.id) + "},");
    //     depth++;
    // }

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
            if (attrName !== "var" && attrs.hasOwnProperty(attrName) && ( !exprs || !exprs.hasOwnProperty(attrName))    )
            {
                commaIfNotFirst();
                indent(ctx, depth + 2);
                const commentOut = propTypes && propTypes[attrName] && (attrName === "var" || propTypes[attrName].client === false);
                if (commentOut)
                {
                    ctx.out.push("/*");
                }
                ctx.out.push(JSON.stringify(attrName), " : ", JSON.stringify(attrs[attrName]));
                if (commentOut)
                {
                    ctx.out.push("*/");
                }
            }
        }
    }

    if (exprs)
    {
        for (let attrName in exprs)
        {
            if (exprs.hasOwnProperty(attrName) && attrName !== RENDERED_IF_PROP)
            {
                const propDecl = propTypes && propTypes[attrName];

                const isCursorOrContextExpression = propDecl && (
                    propDecl.type === "CURSOR_EXPRESSION" ||
                    propDecl.type === "CONTEXT_EXPRESSION"
                );

                const ignoreContextValue = (
                    attrName === VALUE_PROP ||
                    attrName === DISABLED_IF ||
                    attrName === READ_ONLY_IF
                ) && isFormStateComponent(name, componentDescriptor) && !inIterativeContext(ctx,componentModel);

                const commentOut = propDecl && (
                    // we comment out attributes that are marked as client = true
                    propDecl.client === false ||
                    // we also comment out value props for .field components because we supply the values via form-state
                    ignoreContextValue
                );

                if (commentOut)
                {
                    ctx.out.push("\n");
                    indent(ctx, depth + 2);
                    ctx.out.push("/*");
                }
                else
                {
                    commaIfNotFirst();
                    indent(ctx, depth + 2);
                }
                const evaluated = evaluateExpression(componentModel, attrName, true);


                ctx.out.push(JSON.stringify(attrName), " : ");

                if (isCursorOrContextExpression)
                {
                    if (ignoreContextValue)
                    {
                        ctx.out.push(componentModel.attrs[attrName]);
                    }
                    else
                    {
                        const contextName = componentModel.exprs[attrName].contextName;
                        ctx.out.push("_v.cursorExpr(", evaluateExpression(componentModel, MODEL_PROP, commentOut), ",", JSON.stringify(attrName), ",", contextName || "null", ")");
                    }
                }
                else
                {
                    ctx.out.push(evaluated);
                }

                if (commentOut)
                {
                    ctx.out.push("*/");
                }
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


    if (componentDescriptor && componentDescriptor.vars)
    {
        commaIfNotFirst();
        indent(ctx, depth + 2);
        ctx.out.push("vars: _r.getComponentVars(_state, \"" + attrs.id + "\")");
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
        }
    }

    // if (hasInjection)
    // {
    //     ctx.out.push("\n");
    //     indent(ctx, depth);
    //     ctx.out.push(")");
    //     depth--;
    // }
    
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
        "_v.updateState(_state)\n",
//        "console.log('REDUCERS', _r);\n",
        "return (\n\n",
        "  _React.createElement( _ViewWrapper, { title: ", viewModel.titleExpr || "\"\"" , ", store: _store},\n"
    );

    const contentMap = viewModel.content;
    const ctx = new RenderContext(buf, components, contentMap);
    renderRecursively(ctx, contentMap.root, 2);

    //buf.unshift("console.log('COMPONENTS', _c);\n");

    const usedComponents = ctx.usedComponents;
    for (let componentName in usedComponents)
    {
        if (usedComponents.hasOwnProperty(componentName))
        {
            buf.unshift("var " + componentName + " = _c[\"" + componentName + "\"].component;\n");
        }
    }

    buf.push(
        "\n  )\n);"
    );

    return buf.join("");
}

export default {
    createRenderFunction : function(viewModel, components)
    {
        const code = renderViewComponentSource(viewModel, components);
        console.log("\nRENDER-FN:\n", code);

        try
        {
            const renderFn = new Function("_React", "_c", "_v", "_ComponentError", "_sys", "_a", "i18n", "_ViewWrapper", "_r", code);
            return {
                src: code,
                fn: function (component, rtView)
                {
                    return renderFn.call(component, React, components, rtView, ComponentError, sys, rtActionAPI, i18n, ViewWrapper, Reducers);
                }
            };
        }
        catch(e)
        {
            throw new Error("Error compiling view code:\n\n" + code + ": " + e);
        }
    }
};
