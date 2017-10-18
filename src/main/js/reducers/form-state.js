import assign from "object-assign"

import { FORM_FIELD_CHANGE, FORM_ERROR_SET, FORM_FIELD_SYNC } from "../actions/form-state"
import FieldState from "../form/field-state";

export default function( state = null, action)
{
    switch(action.type)
    {
        case FORM_ERROR_SET:
        {
            const newState = assign({}, state);

            const newErrors = assign({}, state.errors);
            if (error !== null)
            {
                newErrors[action.fieldId] = {
                    message: error,
                    formId: action.formId
                };
            }
            else
            {
                newErrors[action.fieldId] = null;
            }
            newState.errors = newErrors;

            return newState;
        }

        case FORM_FIELD_CHANGE:
        {
            const newState = assign({}, state);

            const { fieldId, value, formId } = action;

            const newValues = assign({}, state.fieldValues);
            newValues[fieldId] = value;
            newState.fieldValues = newValues;

            const error = action.error;
            if (error === null)
            {
                newState.errors = state.errors.filter(error => error.fieldId !== fieldId);
            }
            else if (error)
            {
                newState.errors = state.errors.concat({
                    fieldId,
                    formId,
                    message: error,
                });
            }
            return newState;
        }

        case FORM_FIELD_SYNC:
        {
            const newState = assign({}, state);
            const { newFieldValues, newFieldStates } = action;

            if (newFieldValues !== false)
            {
                newState.fieldValues = assign({}, state.fieldValues, newFieldValues);
            }

            if (newFieldStates !== false)
            {
                newState.fieldStates = assign({}, state.fieldStates, newFieldStates);
            }

            return newState;
        }

    }
    return state;
}

export function getFormState(state)
{
    return state.formState;
}

export function getFormErrors(state, formId)
{
    const errors = getFormState(state).errors;
    if (!formId)
    {
        return errors;
    }
    return errors.filter(error => error.formId === formId && fieldInNormalState(state, error.fieldId));
}

export function hasError(state, id)
{
    const errors = getFormState(state).errors;

    for (let i = 0; i < errors.length; i++)
    {
        const error = errors[i];
        if (error.fieldId === id && fieldInNormalState(state, error.fieldId))
        {
            return true;
        }
    }
    return false;
}

function fieldInNormalState(state, fieldId)
{
    return getFieldState(state, fieldId) === FieldState.NORMAL;
}

/**
 * Returns true if the form of the given field contains any errors.
 *
 * @param state         state
 * @param fieldId       fieldId
 * @returns {boolean}
 */
export function formHasError(state, fieldId)
{
    const errors = getFormState(state).errors;

    const { formId } = getField(state, fieldId);

    for (let i = 0; i < errors.length; i++)
    {
        const error = errors[i];
        if (error.formId === formId && fieldInNormalState(state, error.fieldId))
        {
            return true;
        }
    }
    return false;
}

export function getErrorMessage(state, id)
{
    const errors = getFormState(state).errors;

    for (let i = 0; i < errors.length; i++)
    {
        const error = errors[i];
        if (error.fieldId === id)
        {
            return error.message;
        }
    }
    return "";
}

export function getFields(state)
{
    return getFormState(state).fields;
}
export function getField(state, id)
{
    return getFields(state)[id];
}

export function getFieldValues(state)
{
    return getFormState(state).fieldValues;
}
export function getFieldValue(state, id)
{
    return getFieldValues(state)[id];
}

export function getFormBlocks(state)
{
    return getFormState(state).blocks;
}

export function getFormBlock(state, id)
{
    return getFormBlocks(state)[id];
}

export function getFormConfig(state, id)
{
    const field = getField(state, id);

    if (field)
    {
        return field.config;
    }
    else
    {
        return getFormState(state).formConfig[id];
    }
}

export function getFieldStates(state)
{
    return getFormState(state).fieldStates;
}

export function getFieldState(state, id)
{
    return getFieldStates(state)[id];
}

export function getFieldId(state, modelId, cursorPath)
{
    const field = getField(state, modelId);

    if (field.iterationIndex >= 0)
    {
        return modelId + "-" + cursorPath[field.iterationIndex];
    }
    else
    {
        return modelId;
    }
}
