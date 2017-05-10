import React, {Component} from "react"
import cx from "classnames"

import { navigateView } from "../actions/view"

class ActionComponent extends Component {

    static propTypes = {
        defaultClass: React.PropTypes.string.isRequired,
        href: React.PropTypes.string,
        text: React.PropTypes.string,
        icon: React.PropTypes.string,
        onClick: React.PropTypes.func,
        progressive: React.PropTypes.bool
    };

    static defaultProps = {
        icon: "",
        text: "",
        href: "#",
        progressive: true
    };

    onClick = (ev) =>
    {
        try
        {

            if (this.props.onClick)
            {
                if (!this.props.disabled)
                {
                    this.props.onClick(ev);
                }
            }
            else if (this.props.href)
            {
                if (this.props.progressive)
                {
                    navigateView({
                        url: this.props.href
                    });
                }
                else
                {
                    window.location.href = this.props.href;
                }
            }
            else
            {
                console.warn("No target set for Link '" + this.props.text + "'");
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
