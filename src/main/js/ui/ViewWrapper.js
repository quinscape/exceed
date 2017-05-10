
import { connect, Provider } from 'react-redux'

import React from "react"

/**
 * View wrapper component that wraps the components rendered by view-renderer.
 *
 * Currently handles updating the window title.
 */
const ViewWrapper = React.createClass({

    updateTitle: function ()
    {
        document.getElementsByTagName("title")[0].innerHTML = this.props.title || "";
    },

    componentDidMount: function ()
    {
        this.updateTitle();
    },

    componentDidUpdate : function (prevProps, prevState)
    {
        if (prevProps.title !== this.props.title)
        {
            this.updateTitle();
        }
    },

    render: function ()
    {
        return (
            <div>
                { this.props.children }
            </div>
        );
    }
});

module.exports = ViewWrapper;
