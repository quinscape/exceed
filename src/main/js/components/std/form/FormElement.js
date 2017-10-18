/** FormElement is a high order component for bootstrap form fields. It handles all the cursor logic and type analysis and
 * error checking.
 *
 * @param FieldComponent
 * @param [opts]        options
 * @returns {*}
 */
import i18n from "../../../service/i18n";
import store from "../../../service/store";
import FieldState from "../../../form/field-state";
import { changeFormField } from "../../../actions/form-state";
import { getErrorMessage, getField, getFieldValue, getFormConfig, getFieldState, getFieldId } from "../../../reducers/form-state";
import { getCurrency } from "../../../reducers/meta";
import cx from "classnames";
import assign from "object-assign";
import React from "react";
import { undoGroup } from "../../../actions/editor/index";

const DEFAULT_OPTS = {
    decorate: true
};

export default function(FieldComponent, opts)
{
    //console.log("Create FormElement: ", InputComponent.displayName);

    opts = assign({}, DEFAULT_OPTS, opts);

    return class FormElem extends React.Component {

        static displayName = "FormElem(" + ( FieldComponent.displayName || "Unnamed") + ")";

        /**
         * FormElem change handler for two-phase value handling
         *
         * @param value     new value
         * @param final     if set to false, don't propagate the value if the propagate config for the field is set to false
         *                  Field implementations that set this to false *must* ensure that they follow it up with a final = true
         *                  (note that undefined counts as true here)
         */
        onChange = (value, final) =>
        {
            const state = store.getState();
            const { id, propagate }= this.props;

            // for iterative contexts we get the correct cursor as value prop otherwise the correct cursor path is in the field,
            const cursorPath = this.props.value ? this.props.value.getPath() : getField(state, id).cursorPath;

            let fieldId = this.getFieldId();


            store.dispatch(
                changeFormField(
                    fieldId,
                    id,
                    cursorPath,
                    value,
                    final !== false,
                    typeof propagate !== "undefined" ? propagate : getFormConfig(state, id).propagate
                )
            );
        };

        getFieldId()
        {
            const state = store.getState();
            const modelId = this.props.id;
            // for iterative contexts we the correct cursor as value prop, otherwise, the correct cursor path is in the field,
            const cursorPath = this.props.value ? this.props.value.getPath() : getField(state, modelId).cursorPath;

            return getFieldId(state, modelId, cursorPath);
        }

        getInputField()
        {
            return this._input.getInputField();
        }

        render()
        {
            const state = store.getState();
            const modelId = this.props.id;

            const field = getField(state, modelId);

            const { propertyType } = field;

            let fieldId = this.getFieldId();

            const value = getFieldValue(state, fieldId);

            const fieldState = getFieldState(state, fieldId);
            let fieldComponent;
            if (fieldState === FieldState.READ_ONLY)
            {
                fieldComponent = (
                    <p id={ fieldId } className={ cx(this.props.className, "form-control-static") }>
                        { value }
                    </p>
                );
            }
            else
            {
                fieldComponent = (
                    <FieldComponent
                        {... this.props }
                        id={ fieldId }
                        modelId={ modelId }
                        ref={ component => this._input = component }
                        value={ value }
                        onChange={ this.onChange }
                        className={ this.props.className }
                        disabled={ fieldState !== FieldState.NORMAL }
                        propertyType={ propertyType }
                    />
                );
            }


            const errorMessage = fieldState === FieldState.NORMAL && getErrorMessage(state, fieldId);

            const cfg = getFormConfig(state, modelId);

            // some components render their own surrounding markup

            let shouldDecorate = opts.decorate;

            if (typeof opts.decorate === "function")
            {
                shouldDecorate = opts.decorate(propertyType)
            }

            if (!shouldDecorate)
            {
                return fieldComponent;
            }

            if (propertyType.type === "Currency")
            {
                fieldComponent = (
                    <div className="input-group">
                        {
                            fieldComponent
                        }
                        <span className="input-group-addon">
                            {
                                getCurrency(state, propertyType)
                            }
                        </span>
                    </div>
                )
            }

            const labelElement = (
                <label
                    className={
                        cx(
                            "control-label",
                            cfg.horizontal && cfg.labelClass
                        )
                    }
                    htmlFor={
                        fieldId
                    }
                >
                    { this.props.label || i18n(propertyType.parent + ":" + propertyType.name) }
                </label>
            );

            let helpBlock = false;

            if (errorMessage)
            {
                helpBlock = (
                    <p className="text-danger">
                        { errorMessage }
                    </p>
                )
            }



            if (cfg.horizontal)
            {
                fieldComponent = (
                    <div className={ cfg.wrapperClass }>
                        { fieldComponent }
                        { helpBlock }
                    </div>
                )
            }

            const out = (
                <div className={
                    cx(
                        "form-group",
                        errorMessage && "has-error"
                    )
                }>
                    { labelElement }
                    { fieldComponent }
                    { !cfg.horizontal && helpBlock }
                </div>
            );

            return out;
        }
    }
};
