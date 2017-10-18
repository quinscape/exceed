import React, { Component } from "react"
import cx from "classnames"

import store from "../service/store"
import { navigateView } from "../actions/view"

import PropTypes from 'prop-types'

class ActionComponent extends Component {

    static propTypes = {
        defaultClass: PropTypes.string.isRequired,
        href: PropTypes.string,
        text: PropTypes.string,
        icon: PropTypes.string,
        onClick: PropTypes.func,
        progressive: PropTypes.bool
    };

    static defaultProps = {
        icon: "",
        text: "",
        href: "#",
        progressive: true
    };

    onClick = (ev) =>
    {
        const { text, onClick, disabled, href, progressive } = this.props;

        console.log({ text, onClick, disabled, href, progressive });

        try
        {

            if (onClick)
            {
                if (!disabled)
                {
                    onClick(ev);
                }
            }
            else if (href)
            {
                if (progressive)
                {
                    console.log("progressive link", href);
                    store.dispatch(
                        navigateView({
                            url: href
                        })
                    )
                }
                else
                {
                    window.location.href = href;
                }
            }
            else
            {
                console.warn("No target set for Link '" + text + "'");
            }

        }
        catch (err)
        {
            console.error("Error in Link", err);
        }

        ev.preventDefault();
    };

    render()
    {
        const icon = this.props.icon;
        const text = this.props.text;

        const disabled = this.props.disabled;

        let propClasses = this.props.className;

        return (
            React.createElement(disabled ? "span" : "a", {
                    href: this.props.href,
                    className: cx(
                        "btn app-link",
                        !propClasses || propClasses.indexOf("btn-") < 0 ? this.props.defaultClass : null,
                        propClasses,
                        disabled && "disabled"),
                    onClick: this.onClick,
                    accessKey: this.props.accessKey
                },
                icon && <span aria-hidden="true" className={ "glyphicon glyphicon-" + icon }/>,
                text && " " + this.props.text
            )
        );
    }
}

export default ActionComponent
