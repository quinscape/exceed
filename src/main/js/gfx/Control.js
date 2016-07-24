const React = require("react");

const GUIElement = require("../editor/gui/GUIElement");
const UIState = require("../editor/gui/ui-state");


var Control = React.createClass({


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
        var link = this.props.positionLink;
        var pos = link.value;

        return (
            <GUIElement id={ this.props.id } className="control" positionLink={ link } uiState={ UIState.DISABLED } onUpdate={ this.onUpdate }>
                <circle r="10" cx={ pos.x } cy={ pos.y }/>
            </GUIElement>
        );
    }
});

module.exports = Control;
