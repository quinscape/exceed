/**
 * Process transition executing button.
 */
import processService from "../../../service/process";
import store from "../../../service/store";
import cx from "classnames";
import React from "react";
import { formHasError, getFieldState } from "../../../reducers/form-state";
import FieldState from "../../../form/field-state";

import PropTypes from 'prop-types'

class TButton extends React.Component
{
    static propTypes = {
        /** transition to execute */
        transition: PropTypes.string.isRequired,
        /** true if the transition execution discards all user changes / does not depend on field validation */
        discard: PropTypes.bool,
        /** HTML classes */
        className: PropTypes.string,
        /** Text for the button */
        text: PropTypes.string.isRequired,

        mapping: PropTypes.object
    };

    static defaultProps = {
        // default: map any unambiguous object of the target type or throw an error
        mapping : { "?" : "current" }
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

        const { context, discard, transition } = this.props;
        //console.log({ context, discard, transition });

        if (!this.isDisabled())
        {

            let objects = null;

            if (!discard && context && context.getGraph().qualifier === "QUERY")
            {
                objects = context.extractObjects();
            }

            processService.transition(
                transition,
                objects
            )
            // .catch(function(err)
            // {
            //     console.log("TRANSITION FAIL");
            //     console.error(err);
            // });
        }
    };

    render ()
    {
//        console.log("RENDER TBUTTON", this.props.context && this.props.context.graph.id);
        const { className, text, icon } = this.props;

        const isDisabled = this.isDisabled();
        return (

            <button
                type="button"
                className={
                    cx(
                        "btn",
                        isDisabled && "disabled",
                        className || "btn-default"
                    )
                }
                value={ text }
                disabled={ isDisabled }
                onClick={ this.onClick }
            >

                { icon && <span className={ "glyphicon glyphicon-" + icon }/> }
                { " " + text }
            </button>
        );
    }
}

export default TButton
