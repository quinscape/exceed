import React from "react";
import ActionComponent from "./ActionComponent";

import PropTypes from 'prop-types'

class Button extends React.Component
{
    static propTypes = {
        text: PropTypes.string,
        icon: PropTypes.string,
        onClick: PropTypes.func
    };

    render()
    {
        return (
            <ActionComponent {... this.props} defaultClass="btn-default" />
        );
    }
}

export default Button
