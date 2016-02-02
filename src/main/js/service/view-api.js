var React = require("react");

var security = require("./security");

var InPageEditor = false;
if (process.env.NODE_ENV !== "production")
{
    InPageEditor = require("../editor/InPageEditor");
}

function RTView(model, data)
{
    this.name = model.name;
    this.root = model.root;
    this.data = data;
}

RTView.prototype.inject = function (props, data)
{
    for (var k in data)
    {
        if (data.hasOwnProperty(k) && !props.hasOwnProperty(k))
        {
            props[k] = data[k];
        }
    }

    return props;
};

var viewAPI = {
    RTView: RTView,
    editor: function(model)
    {
        return (
            security.hasRole("ROLE_EDITOR") && InPageEditor && <InPageEditor model={ model } />
        );
    },
    error: function (model, e)
    {
        //console.error("Error rendering view", e);

        return (
            <div>
                <div className="bg-danger">
                    <pre>
                        { String(e) }
                    </pre>
                    <pre>
                        { String(e.stack) }
                    </pre>
                </div>
                { viewAPI.editor(model) }
            </div>
        );
    }
};
module.exports = viewAPI;
