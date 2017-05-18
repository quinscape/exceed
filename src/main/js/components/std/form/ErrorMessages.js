import FormContext from "../../../util/form-context";
import i18n from "../../../service/i18n";
import keys from "../../../util/keys";
import React from "react";


class ErrorMessages extends React.Component
{
    static contextTypes = {
        formContext: React.PropTypes.instanceOf(FormContext)
    }

    componentDidMount ()
    {
        var ctx = this.context.formContext;
        ctx._errorMessages = this;
    }

    componentWillUnmount ()
    {
        var ctx = this.context.formContext;
        ctx._errorMessages = null;
    }

    render ()
    {
        var ctx = this.context.formContext;
        if (!ctx.hasError())
        {
            return false;
        }

        var errors = ctx.errors;

//        console.log("ERRORS", errors);

        var names = keys(errors).sort();

        return (
            names.length &&
            <div className="error-messages form-group has-error">
                <h4>{ i18n("Errors") }</h4>
                <ul className="errors">
                {
                    names.map(id =>
                        !!errors[id] && (
                            <li key={ id } >
                                <label className="control-label" htmlFor={ id }>
                                    { errors[id] }
                                </label>
                            </li>
                        )
                    )
                }
                </ul>
            </div>
        );
    }
};

export default ErrorMessages
