var React = require("react/addons");
var extend = require("extend");

var security = require("../service/security");

var InPageEditor = require("../editor/InPageEditor");

var components = require("../service/component").getComponents();

var expressionRegEx = /^\{\s*(.*?)\s*}$/;

var ViewComponent = React.createClass({
    renderComponent: function (model, componentData, key)
    {
        //console.log("renderComponent", model, componentData, key);

        var component = model.name;

        var modelAttrs = model.attrs;
        var modelId = modelAttrs && model.id;
        if (component <= 'Z')
        {
            component = components[component].component;
        }

        var m;
        var attrs = {
            key: key,
            id: modelId
        };

        if(modelAttrs)
        {
            for (var name in modelAttrs)
            {
                if (modelAttrs.hasOwnProperty(name))
                {
                    var value = modelAttrs[name];
                    if (typeof value === "string" && (m = expressionRegEx.exec(value)))
                    {
                        value = eval(value);
                    }
                    attrs[name] = value;
                }
            }
        }
        var modelKids = model.kids;
        var callArgs = [ component, extend(attrs, modelId && components[modelId]) ];

        if (modelKids)
        {
            for (var i = 0; i < modelKids.length; i++)
            {
                var kid = modelKids[i];

                callArgs.push(this.renderComponent(kid, componentData, i));
            }
        }

        //console.log("createElement", callArgs, modelKids);
        return React.createElement.apply(React, callArgs);
    },

    render: function ()
    {
        var model = this.props.model;

        return (
            <div className="container-fluid">
                <div className="row">
                    <div className="col-md-12">
                        { this.renderComponent(model.root, this.props.componentData, "root") }
                    </div>
                </div>
                { security.hasRole("ROLE_EDITOR") && <InPageEditor model={ model } activeLink={ this.props.activeLink } /> }
            </div>
        );
    }
});

module.exports = ViewComponent;
