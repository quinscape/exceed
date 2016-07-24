const React = require("react");
const cx = require("classnames");

module.exports = function (name, defaultClass)
{
    return React.createClass({

        displayName: name,

        propTypes: {
            text: React.PropTypes.string,
            icon: React.PropTypes.string,
            onClick: React.PropTypes.func
        },

        getDefaultProps: function ()
        {
            return {
                icon: "",
                text: ""
            }
        },

        onClick: function (ev)
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
                else
                {
                    console.warn("No onClick for Link '" + this.props.text + "'");
                }

            }
            catch (err)
            {
                console.error("Error in Link", err);
            }

            ev.preventDefault();
        },

        render: function ()
        {
            var icon = this.props.icon;
            var text = this.props.text;

            var disabled = this.props.disabled;

            var propClasses = this.props.className;

            return (
                React.createElement(disabled ? "span" : "a", {
                        href: this.props.href || "#",
                        className: cx(
                            "btn app-link",
                            !propClasses || propClasses.indexOf("btn-") < 0 ? defaultClass : null,
                            propClasses,
                            disabled && "disabled"),
                        onClick: this.onClick,
                        accessKey: this.props.accessKey
                    },
                    icon && <span className={ "glyphicon glyphicon-" + icon } aria-hidden="true"/>,
                    text && " " + this.props.text
                )
            );
        }
    });

};
