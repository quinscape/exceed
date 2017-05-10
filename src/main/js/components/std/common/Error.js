import React, { Component } from "react"

import ErrorReport from "../../../ui/ErrorReport.es5"

class Error extends Component
{
    render()
    {
        return (
            <ErrorReport error={ this.props.error } />
        );
    }
}

export default Error
