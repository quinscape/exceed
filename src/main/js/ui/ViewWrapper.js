
import { connect, Provider } from 'react-redux'

import React from "react"

/**
 * View wrapper component that wraps the components rendered by view-renderer.
 *
 * Currently handles updating the window title.
 */
class ViewWrapper extends React.Component
{

    updateTitle()
    {
        document.getElementsByTagName("title")[0].innerHTML = this.props.title || "";
    }

    componentDidMount()
    {
        this.updateTitle();
    }

    componentDidUpdate(prevProps, prevState)
    {
        if (prevProps.title !== this.props.title)
        {
            this.updateTitle();
        }
    }

    render()
    {
        return (
            <div>
                { this.props.children }
            </div>
        );
    }
}

export default ViewWrapper
