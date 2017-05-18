import React from "react";
import ActionComponent from "./ActionComponent";


class Link extends React.Component
{

    static propTypes = {
        text: React.PropTypes.string,
        icon: React.PropTypes.string,
        href: React.PropTypes.string,
        onClick: React.PropTypes.func,
        progressive: React.PropTypes.bool
    }

    render()
    {
        return (
            <ActionComponent {... this.props} defaultClass="btn-link" />
        );
    }
}

export default Link
