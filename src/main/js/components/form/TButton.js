    var React = require("react");
var cx = require("classnames");

var i18n  = require("../../service/i18n");

var processService  = require("../../service/process");

var FormContext = require("../../util/form-context");

/**
 * Process transition executing button.
 */
var TButton = React.createClass({

    propTypes: {
        /** transition to execute */
        transition: React.PropTypes.string.isRequired,
        /** true if the transition execution discards all user changes / does not depend on field validation */
        discard: React.PropTypes.bool,
        /** HTML classes */
        className: React.PropTypes.string,
        /** Text for the button */
        text: React.PropTypes.string.isRequired,
        /** Domain type to extract as context for the transition. Can be undefined if the context is unambiguous */
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
                        var domainObject = null;
                        var cursor = this.props.context;
                        if (!this.props.discard && cursor)
                        {
                            if (cursor.isProperty())
                            {
                                cursor = cursor.pop();
                            }
                            domainObject = cursor.getDomainObject(this.props.domainType);
                        }

                        processService.transition( this.props.transition, domainObject)
                            .catch(function(err)
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
