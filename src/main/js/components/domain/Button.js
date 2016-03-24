var React = require("react");
var cx = require("classnames");

var i18n  = require("../../service/i18n");
var actionService  = require("../../service/action");

var FormContext = require("./form-context");

/**
 * Action executing button
 */
var Button = React.createClass({

    propTypes: {
        action: React.PropTypes.func.isRequired,
        ignoreErrors: React.PropTypes.bool,
        className: React.PropTypes.string,
        text: React.PropTypes.string.isRequired
    },

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    getInitialState: function ()
    {
        var ctx = this.context.formContext;

        return {
            id: ctx.nextId()
        };
    },

    isDisabled: function ()
    {
        return !this.props.ignoreErrors && this.context.formContext.hasError()

    },

    render: function ()
    {
        var formContext = this.context.formContext;
        var dataList = formContext.dataList;
        var id = this.state.id;
        var isDisabled = this.isDisabled();
        return (

            <input
                id={ id }
                name={ id }
                className={ cx("btn", isDisabled && "disabled", this.props.className || "btn-default") }
                type="submit"
                value={ this.props.text }
                disabled={ isDisabled }
                onClick={ (ev) => {

                    if (!this.isDisabled())
                    {
                        actionService.execute( this.props.action, dataList.getRaw());
                    }
                    ev.preventDefault();
                } }/>
        );
    }
});

module.exports = Button;
