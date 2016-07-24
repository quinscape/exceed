var React = require("react");

var FormContext = require("../../util/form-context");

var FormBlock = React.createClass({

    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    propTypes: {
        horizontal: React.PropTypes.bool,
        labelClass: React.PropTypes.string,
        wrapperClass: React.PropTypes.string
    },


    getChildContext: function()
    {
        var ctx = this.context.formContext;

        var newContext = new FormContext(
            this.props.horizontal || ctx.horizontal,
            this.props.labelClass || ctx.wrapperClass(),
            this.props.wrapperClass || ctx.labelClass()
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
