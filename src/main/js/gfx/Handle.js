const React = require("react");

const GUIElement = require("../editor/gui/GUIElement");
const UIState = require("../editor/gui/ui-state");

/**
 * Drag and Drop control handle for graph arrows bezier curves.
 */
var Handle = React.createClass({


    onUpdate: function ()
    {
        this.forceUpdate();
    },

    //shouldComponentUpdate: function (nextProps)
    //{
    //    return (
    //        this.props.x !== nextProps.x ||
    //        this.props.y !== nextProps.y
    //    );
    //},

    render: function ()
    {
        var pos = this.props.position;

        return (
            <GUIElement
                id={ this.props.id }
                className="control"
                position={ this.props.position }
                updatePosition={ this.props.updatePosition }
                uiState={ UIState.DISABLED }
                onUpdate={ this.onUpdate }
            >
                <circle r="10" cx={ pos.x } cy={ pos.y }/>
            </GUIElement>
        );
    }
});

module.exports = Handle;
