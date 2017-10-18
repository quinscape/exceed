import React from "react";

import PropTypes from 'prop-types'

class Alert extends React.Component
{
    static propTypes =  PropTypes.string.isRequired;

    render()
    {
        return (
            <div className="bg-danger">
                { this.props.message }
            </div>
        );
    }
}

export default Alert
