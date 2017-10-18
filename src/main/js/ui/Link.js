import React from "react";
import ActionComponent from "./ActionComponent";
import PropTypes from 'prop-types'


class Link extends React.Component
{

    static propTypes = {
        text: PropTypes.string,
        icon: PropTypes.string,
        href: PropTypes.string,
        onClick: PropTypes.func,
        progressive: PropTypes.bool
    }

    render()
    {
        return (
            <ActionComponent {... this.props} defaultClass="btn-link" />
        );
    }
}

export default Link
