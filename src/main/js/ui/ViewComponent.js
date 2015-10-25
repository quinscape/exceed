var React = require("react/addons");
var extend = require("extend");

var security = require("../service/security");

var InPageEditor = require("../editor/InPageEditor");

var Alert = require("../ui/Alert");

var components = require("../service/component").getComponents();

var expressionRegEx = /^\{\s*(.*?)\s*}$/;

function evaluateExpression(value, context)
{
    var m;
    if (typeof value === "string" && (m = expressionRegEx.exec(value)))
    {
        return eval(m[1]);
    }
    return value;
}

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
                return evaluateExpression(model.attrs.value, context);
            }

            var modelAttrs = model.attrs;
            var attrs = {
                key: key
            };

            var modelId = modelAttrs && modelAttrs.id;
            if (component <= 'Z')
            {
                descriptor = components[component];

                //console.log("descriptor = ", descriptor)

                component = descriptor.component;

                attrs.model = model;
            }

            var m;

            if (modelAttrs)
            {
                for (var name in modelAttrs)
                {
                    if (modelAttrs.hasOwnProperty(name) && name !== "id")
                    {
                        attrs[name] = evaluateExpression(modelAttrs[name], context);
                    }
                }
            }

            var contextKey = descriptor && descriptor.contextKey;
            if (contextKey)
            {
                context = context[attrs[contextKey]];
            }

            attrs.context = context;

            var modelKids = model.kids;

            if (modelId)
            {
                var dataBlock = componentData[modelId];

                if (dataBlock)
                {
                    if (dataBlock.vars)
                    {
                        attrs.vars = dataBlock.vars;
                    }

                    if (dataBlock.data)
                    {
                        extend(attrs, dataBlock.data);
                        //console.log("data = ", JSON.stringify(dataBlock.data, null, "  "));
                    }
                }
            }

            if (descriptor && descriptor.contextProvider && modelKids)
            {
                var viewComponent = this;

                attrs.childCount = modelKids.length;
                attrs.renderChildrenWithContext = function (context)
                {
                    var array = [];
                    for (var i = 0; i < modelKids.length; i++)
                    {
                        var kid = modelKids[i];
                        array[i] = viewComponent.renderComponent(kid, componentData, i, context);
                    }
                    return array;
                };
                callArgs = [component, attrs];
            }
            else
            {
                callArgs = [component, attrs];
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

module.exports = ViewComponent;
