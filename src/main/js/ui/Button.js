import React from "react";
import ActionComponent from "./ActionComponent";


class Button extends React.Component
{
    static propTypes = {
        text: React.PropTypes.string,
        icon: React.PropTypes.string,
        onClick: React.PropTypes.func
    }

    render()
    {
        return (
            <ActionComponent {... this.props} defaultClass="btn-default" />
        );
    }
}

export default Button
