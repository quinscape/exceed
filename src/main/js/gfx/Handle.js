const React = require("react");

import GUIElement from "../editor/gui/GUIElement"
import UIState from "../editor/gui/ui-state";

/**
 * Drag and Drop control handle for graph arrows bezier curves.
 */
class Handle extends React.Component
{
    onUpdate()
    {
        this.forceUpdate();
    }

    //shouldComponentUpdate: function (nextProps)
    //{
    //    return (
    //        this.props.x !== nextProps.x ||
    //        this.props.y !== nextProps.y
    //    );
    //},

    render()
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
}

export default Handle
