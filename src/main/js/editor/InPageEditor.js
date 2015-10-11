var React = require("react/addons");

var classes = require("classnames");

const EDITOR_STORAGE_KEY = "InPageEditorState";


var InPageEditor = React.createClass({
    getInitialState: function ()
    {
        var item = sessionStorage.getItem(EDITOR_STORAGE_KEY);

        return item ? JSON.parse(item) : {
            active: false
        };
    },
    componentWillUpdate: function (nextProps, nextState)
    {
        sessionStorage.setItem(EDITOR_STORAGE_KEY, JSON.stringify(nextState));
    },
    toggle: function ()
    {
        this.setState({
            active: !this.state.active
        })
    },
    render: function ()
    {
        return (
                <div className={ classes("editor", this.state.active ? "active" : "inactive") }>
                    <div className="editor-tab" onClick={ this.toggle }>E</div>
                    { this.state.active &&

                        <div className="container-fluid">
                            <h1>Editor</h1>
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
