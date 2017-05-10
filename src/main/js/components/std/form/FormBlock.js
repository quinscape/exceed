import FormContext from "../../../util/form-context";
const React = require("react");

const FormBlock = React.createClass({

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    propTypes: {
        horizontal: React.PropTypes.bool,
        labelClass: React.PropTypes.string,
        wrapperClass: React.PropTypes.string
    },

    getChildContext: function ()
    {
        const ctx = this.context.formContext;

        const newContext = new FormContext(
            this.props.horizontal || ctx.horizontal,
            this.props.labelClass || ctx.wrapperClass(),
            this.props.wrapperClass || ctx.labelClass(),
            this.props.update || ctx.update
        );

        newContext.errors = ctx.errors;

        return {
            formContext: newContext
        };
    },

    getDefaultProps: function ()
    {
        return {
            horizontal: false,
            labelClass: "col-md-2",
            wrapperClass: "col-md-4"
        };
    },

    render: function ()
    {
        return (
            <div className="form-block">
                { this.props.children }
            </div>
        );
    }
});

module.exports = FormBlock;
