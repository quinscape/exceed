var React = require("react");
var cx = require("classnames");

/**
 * Button for transitions / process-based content
 */
var TButton = React.createClass({

    propTypes: {
        transition: React.PropTypes.string.isRequired,
        className: React.PropTypes.string,
        text: React.PropTypes.string.isRequired
    },

    render: function ()
    {
        return (
            <TButton
                className={ this.props.ClassName }
                text={ this.props.text }
                action={ { action: "transition", name: this.props.transition} }
            />
        );
    }
});

module.exports = TButton;
