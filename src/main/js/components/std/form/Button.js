/**
 * Action executing button
 */
import cx from "classnames";
import React from "react";
import { formHasError, getFieldState } from "../../../reducers/form-state";
import store from "../../../service/store";
import FieldState from "../../../form/field-state";

import PropTypes from 'prop-types'

class Button extends React.Component
{

    static propTypes = {
        action: PropTypes.func.isRequired,
        discard: PropTypes.bool,
        className: PropTypes.string,
        text: PropTypes.string.isRequired
    };

    isDisabled ()
    {
        const { id, discard} = this.props;

        if (discard)
        {
            return false;
        }

        const state = store.getState();

        const hasError = formHasError(state, id);
        const isDisabledByExpr = getFieldState(state, id) !== FieldState.NORMAL;

        return hasError || isDisabledByExpr;
    }

    onClick = ev => {

        const { action } = this.props;

        if (!this.isDisabled())
        {
            action();
        }

    };

    render ()
    {
        const { id, className, title, icon, text } = this.props;
        const isDisabled = this.isDisabled();

        return (

            <button
                type="button"
                id={ id }
                name={ id }
                className={ cx("btn", isDisabled && "disabled", className || "btn-default") }
                title={ title }
                disabled={ isDisabled }
                onClick={ this.onClick }
            >
                { icon && <span className={ "glyphicon glyphicon-" + icon }></span>}
                { " " + text }
            </button>
        );
    }
}

export default Button;
