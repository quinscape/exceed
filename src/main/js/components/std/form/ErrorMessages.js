import FormContext from "../../../util/form-context";
const React = require("react");

const keys = require("../../../util/keys");
const i18n = require("../../../service/i18n");

var ErrorMessages = React.createClass({
    contextTypes: {
        formContext: React.PropTypes.instanceOf(FormContext)
    },

    componentDidMount: function ()
    {
        var ctx = this.context.formContext;
        ctx._errorMessages = this;
    },

    componentWillUnmount: function ()
    {
        var ctx = this.context.formContext;
        ctx._errorMessages = null;
    },

    render: function ()
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
});

module.exports = ErrorMessages;
