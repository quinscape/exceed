import i18n from "../../../service/i18n";
import store from "../../../service/store";
import React from "react";

import { getFormErrors } from "../../../reducers/form-state";
import { findParent, isFormComponent } from "../../../util/component-util";


class ErrorMessages extends React.Component
{
    render ()
    {
        const state = store.getState();
        const formComponent = findParent(this.props.model, isFormComponent);

        const errors = getFormErrors(state, formComponent ? formComponent.attrs.id : null);
        console.log("ERRORS", errors);

        return (
            !!errors.length &&
            <div className="error-messages form-group has-error">
                <h4>{ i18n("Errors") }</h4>
                <ul className="errors">
                {
                    errors.map( (error, index) =>
                        error && (
                            <li key={ index } >
                                <label className="control-label" htmlFor={ error.fieldId }>
                                    { error.message }
                                </label>
                            </li>
                        )
                    )
                }
                </ul>
            </div>
        );
    }
}

export default ErrorMessages
