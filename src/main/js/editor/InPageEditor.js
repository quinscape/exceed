var React = require("react");

var classes = require("classnames");
var assign = require("object.assign").getPolyfill();
var ValueLink = require("../util/value-link");

var SVGLayout = require("../util/svg-layout");

var CodeEditor = require("./code/CodeEditor");

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

var editorTabs = {
    "current" : React.createClass({
        displayName: "CurrentModel",
        render: function ()
        {

            return (
                <div style={{
                    paddingLeft: "1px",
                    paddingBottom: "1px"
                }}>
                    <CodeEditor model={ this.props.model } />
                </div>
            );
        }
    }),
    "domain" : require("./DomainEditor")
};

function TabLink(props)
{
    var tabLink = props.tabLink;

    var isCurrent = tabLink.value === props.tab;
    return (

        React.createElement(isCurrent ? "span" : "a", {
            className: classes("btn", "btn-link", isCurrent && "disabled"),
            'data-tab' : props.tab,
            href: "#" + props.tab,
            onClick: function (ev)
            {
                props.tabLink.requestChange(ev.target.dataset.tab);
                ev.preventDefault();

            }
        }, props.text)

    );
}

const DEFAULT_STATE = {
    active: false,
    tab: "current"
};

var InPageEditor = React.createClass({
    getInitialState: function ()
    {
        var savedState = sessionStorage.getItem(EDITOR_STORAGE_KEY);

        if (savedState)
        {
            return assign({}, DEFAULT_STATE,  JSON.parse(savedState));
        }
        else
        {
            return DEFAULT_STATE;
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
    changeTab: function (newValue)
    {
        this.setState({
            tab: newValue
        });

    },
    render: function ()
    {
        var tabLink = new ValueLink(this.state.tab, this.changeTab);

        var Tab = editorTabs[this.state.tab];
        return (
                <div className={ classes("editor", this.state.active ? "active" : "inactive") }>
                    <a className="editor-tab" onClick={ this.toggle } href="#toggle-editor" accessKey="e">E</a>
                    { this.state.active &&
                        <div className="editor-body">
                            <div className="editor-toolbar toolbar">
                                <TabLink tab="current" text="Current Model" tabLink={ tabLink } />
                                <TabLink tab="domain" text="Domain" tabLink={ tabLink } />
                            </div>
                            <Tab {... this.props}/>
                        </div>
                    }
                </div>
        );
    }
});

module.exports = InPageEditor;
