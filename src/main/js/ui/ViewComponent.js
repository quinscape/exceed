var React = require("react");
var extend = require("extend");

var security = require("../service/security");

var InPageEditor = require("../editor/InPageEditor");

var Alert = require("../ui/Alert");

var components = require("../service/component").getComponents();

var expressionRegEx = /^\{\s*(.*?)\s*}$/;


/**
 * A generic view component that renders a component tree based on a JSON view notation.
 *
 * {
 *     "root": {
 *         "name": "Bootstrap.Grid",
 *         "attrs": {
 *             "fluid": "{ true }"
 *         },
 *         "kids": [ … ]
 * }
 *
 * "root" contains the root component. For each component there can be 3 properties:
 *
 * "name" contains the component name as registered in the exceed build, parallel to React conventions we reserve
 * lowercase names for normal HTML elements. String children use the name "[String]".
 *
 * "attrs" contains a map of attributes that are either string values or an expression string wrapped in {}. ViewComponent
 * just evaluates the expression in a special environment relying on server-side validation to make sure that the
 * expression string is actually valid.
 *
 *
 */
var ViewComponent = React.createClass({
    renderComponent: function (model, componentData, key, context)
    {
        try
        {
            //console.log("renderComponent", model, componentData, key);

            var descriptor;
            var callArgs;
            var component = model.name;

            if (component === "[String]")
            {
                return evaluateExpression(model.attrs.value, context, model, null, null);
            }

            var modelAttrs = model.attrs;
            var componentProps = {
                key: key
            };

            if (isComponent(component))
            {
                descriptor = components[component];

                //console.log("descriptor = ", descriptor)

                component = descriptor.component;

                if (descriptor.modelAware)
                {
                    componentProps.model = model;
                }
            }

            var modelId = modelAttrs && modelAttrs.id;
            if (modelId)
            {
                var dataBlock = componentData[modelId];

                if (dataBlock)
                {
                    if (dataBlock.vars)
                    {
                        componentProps.vars = dataBlock.vars;
                    }

                    if (dataBlock.data)
                    {
                        extend(componentProps, dataBlock.data);
                        //console.log("data = ", JSON.stringify(dataBlock.data, null, "  "));
                    }
                }
            }

            var contextKey = descriptor && descriptor.contextKey;
            if (contextKey)
            {
                var k = modelAttrs[contextKey];
                context = context[k];
            }

            if (context && (descriptor.context || descriptor.contextKey))
            {
                var contextProperty =  typeof descriptor.context === "string" ? descriptor.context : "context";
                componentProps[contextProperty] = context;
            }

            if (modelAttrs)
            {
                for (var name in modelAttrs)
                {
                    if (modelAttrs.hasOwnProperty(name) && name !== "id")
                    {
                        componentProps[name] = evaluateExpression(modelAttrs[name], context, model, componentProps, componentProps.vars);
                    }
                }
            }

            var modelKids = model.kids;
            if (descriptor && descriptor.contextProvider && modelKids)
            {
                var viewComponent = this;

                componentProps.childCount = modelKids.length;
                componentProps.renderChildrenWithContext = function (context)
                {
                    var array = [];
                    for (var i = 0; i < modelKids.length; i++)
                    {
                        var kid = modelKids[i];
                        array[i] = viewComponent.renderComponent(kid, componentData, i, context);
                    }
                    return array;
                };
                callArgs = [ component, componentProps ];
            }
            else
            {
                callArgs = [ component, componentProps ];
                if (modelKids)
                {
                    for (var i = 0; i < modelKids.length; i++)
                    {
                        var kid = modelKids[i];

                        callArgs.push(this.renderComponent(kid, componentData, i, context));
                    }
                }
            }

            //console.log("createElement", component, JSON.stringify(callArgs[1], function(k,v) { if (k === "model") return undefined; else return v;  }));
            return React.createElement.apply(React, callArgs);
        }
        catch (e)
        {
            return ( <Alert message={ "Error rendering <" + model.name + " id='" + modelId + "'/>: " + e  }/> );
        }
    },

    render: function ()
    {
        var model = this.props.model;

        return (
            <div className="container-fluid">
                <div className="row">
                    <div className="col-md-12">
                        { this.renderComponent(model.root, this.props.componentData, 0, null) }
                    </div>
                </div>
                { security.hasRole("ROLE_EDITOR") && <InPageEditor model={ model } activeLink={ this.props.activeLink } /> }
            </div>
        );
    }
});

function evaluateExpression(value, context, model, props, vars)
{
    var m;
    if (typeof value === "string" && (m = expressionRegEx.exec(value)))
    {
        return eval("(" + m[1] + ")");
    }
    return value;
}

function isComponent(component)
{
    return component <= 'Z';
}

module.exports = ViewComponent;
