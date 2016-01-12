var React = require("react");

var classes = require("classnames");

const EDITOR_STORAGE_KEY = "InPageEditorState";

function Indicator(props)
{
    var mode = props.mode || "success";

    return (
        <span className={ classes("indicator",  "bg-" + mode,  "text-" + mode) }>
            { props.text || "\u00a0" }
        </span>
    );
}

var InPageEditor = React.createClass({
    getInitialState: function ()
    {
        var savedState = sessionStorage.getItem(EDITOR_STORAGE_KEY);

        if (savedState)
        {
            return JSON.parse(savedState);
        }
        else
        {
            return {
                active: false
            };
        }
    },
    componentWillUpdate: function (nextProps, nextState)
    {
        sessionStorage.setItem(EDITOR_STORAGE_KEY, JSON.stringify(nextState));
    },
    toggle: function (ev)
    {
        this.setState({
            active: !this.state.active
        });

        ev.preventDefault();
    },
    render: function ()
    {
        return (
                <div className={ classes("editor", this.state.active ? "active" : "inactive") }>
                    <a className="editor-tab" onClick={ this.toggle } href="#toggle-editor" accessKey="e">E</a>
                    { this.state.active &&
                        <div className="editor-body">
                            <div className="editor-toolbar toolbar">
                                <span className="btn btn-link disabled" href="#domain">Current View</span>
                                <a className="btn btn-link" href="#domain">Domain</a>
                                <a className="btn btn-link" href="#domain">Structure</a>
                            </div>
                            <pre>
                                { JSON.stringify(this.props.model, null, "    ")}
                            </pre>
                        </div>
                    }
                </div>
        );
    }
});

module.exports = InPageEditor;
