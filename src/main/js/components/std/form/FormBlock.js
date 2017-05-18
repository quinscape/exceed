import FormContext from "../../../util/form-context";
import React from "react";

class FormBlock extends React.Component
{

    static contextTypes = {
        formContext: React.PropTypes.instanceOf(FormContext)
    }

    static propTypes = {
        horizontal: React.PropTypes.bool,
        labelClass: React.PropTypes.string,
        wrapperClass: React.PropTypes.string
    }

    getChildContext ()
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
    }

    getDefaultProps ()
    {
        return {
            horizontal: false,
            labelClass: "col-md-2",
            wrapperClass: "col-md-4"
        };
    }

    render ()
    {
        return (
            <div className="form-block">
                { this.props.children }
            </div>
        );
    }
};

export default FormBlock
