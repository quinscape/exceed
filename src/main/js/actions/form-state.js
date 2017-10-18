export const FORM_ERROR_SET = "FORM_ERROR_SET";
export const FORM_FIELD_CHANGE = "FORM_FIELD_CHANGE";
export const FORM_FIELD_SYNC = "FORM_FIELD_SYNC";

import propertyConverter from "../service/property-converter"
import { getErrorMessage, getField, getFormConfig, getFieldValues } from "../reducers/form-state"
import { updateScope } from "../actions/scope"

export function signalError(fieldId, message)
{
    return (dispatch, getState) => {
        dispatch({
            type: FORM_ERROR_SET,
            fieldId,
            message,
            formId: getField(getState(), fieldId)
        });
    }
}

export function changeFormField(fieldId, modelId, cursorPath, value, isFinal, propagate)
{
    return (dispatch, getState) => {

        const state = getState();
        const field = getField(state,modelId);

        const currentValue = getFieldValues(state)[fieldId];
        let isChanged = currentValue !== value;
        if (isChanged || isFinal)
        {
            const result = propertyConverter.fromUser(value, field.propertyType);
            let isOk = result.ok;

            // if this is a final update which is ok
            if (isFinal && isOk)
            {
                // norm the final value to the reconverted property value
                value = propertyConverter.toUser(result.value, field.propertyType);
                // if it has changed now
                isChanged = currentValue !== value;
            }

            if (isChanged)
            {
                // -> update form field

                const currentError = getErrorMessage(state, fieldId);
                let error = false;
                if (isOk && currentError)
                {
                    error = null;
                }
                else if (!isOk && currentError !== error)
                {
                    error = result.error;
                }

                dispatch({
                    type: FORM_FIELD_CHANGE,
                    fieldId,
                    value,
                    error,
                    formId: field.formId
                });
            }

            if (isOk && (propagate || isFinal))
            {
                // -> update dependent scope values
                dispatch(
                    updateScope(
                        cursorPath,
                        result.value
                    )
                )
            }
        }
    };
}

/**
 * Synchronizes multiple form field values and field states in one action. It is used to synchronize changes back
 * from changed scope values and hence does *not* propagate its changes to the scope.
 *
 * @param newFieldValues    
 * @param newFieldStates
 * @returns {{type: string, newFieldValues: *, newFieldStates: *}}
 */
export function syncFormFields(newFieldValues, newFieldStates)
{
    return {
        type: FORM_FIELD_SYNC,
        newFieldValues,
        newFieldStates
    };
}
