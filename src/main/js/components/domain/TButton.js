var React = require("react");
var cx = require("classnames");

var i18n  = require("../../service/i18n");

var processService  = require("../../service/process");

var FormContext = require("./form-context");

/**
 * Action executing button
 */
var TButton = React.createClass({

    propTypes: {
        transition: React.PropTypes.string.isRequired,
        discard: React.PropTypes.bool,
        className: React.PropTypes.string,
        text: React.PropTypes.string.isRequired,
        domainType: React.PropTypes.string
    },

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    isDisabled: function ()
    {
        return !this.props.discard && this.context.formContext && this.context.formContext.hasError()

    },

    render: function ()
    {
        var isDisabled = this.isDisabled();
        return (

            <input
                className={ cx("btn", isDisabled && "disabled", this.props.className || "btn-default") }
                type="submit"
                value={ this.props.text }
                disabled={ isDisabled }
                onClick={ (ev) => {
                    if (!this.isDisabled())
                    {
                        var cursor = this.props.context;
                        if (cursor && cursor.isProperty())
                        {
                            cursor = cursor.pop();
                        }

                        const domainObject = cursor && cursor.getDomainObject(this.props.domainType);

                        processService.transition( this.props.transition, domainObject).catch(function(err)
                        {
                            console.error(err);
                        });
                    }
                    ev.preventDefault();
                } }/>
        );
    }
});

module.exports = TButton;
