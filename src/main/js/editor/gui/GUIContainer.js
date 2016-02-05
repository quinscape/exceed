var React = require("react");
var GUIContext = require("./gui-context");

/**
 * Contains SVG based GUIElement and provides a GUIContext context object to GUIElement instances to manage SVG
 * component focus and keyboard interaction via focus proxy elements.
 */
var GUIContainer = React.createClass({
    componentDidMount: function ()
    {
        GUIContext.update();
    },

    render: function ()
    {
        return (
            <div className="gui-container">
                <svg width={ this.props.width || "100%" } height={ this.props.height || 600 }>
                    { this.props.children }
                </svg>
            </div>
        );
    }
});


module.exports = GUIContainer;
