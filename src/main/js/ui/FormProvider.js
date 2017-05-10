import React from "react";
import FormContext from "../util/form-context";

class FormProvider extends React.Component {

    static childContextTypes = {
        formContext: React.PropTypes.instanceOf(FormContext)
    };

    state = {
        errors: {}
    };

    static defaultProps = {
        labelClass: "col-md-2",
        wrapperClass: "col-md-4"
    };

    getChildContext()
    {
        return {
            formContext: new FormContext(
                this.props.horizontal || false,
                this.props.labelClass,
                this.props.wrapperClass,
                this.props.update
            )
        };
    }

    render()
    {
        const { children } = this.props;

        return (
            <div>
                { children }
            </div>
        );
    }
}

export default FormProvider
