import DataCursor from "../domain/cursor"
import { getScopeGraph } from "../reducers/scope"
import { getDomainData, getViewModel } from "../reducers/meta"
import { updateComponent } from "../actions/component"
import { getComponentVars } from "../reducers/component"
import { syncFormFields } from "../actions/form-state"
import { getFields, getFieldValues, getFormBlocks, getFieldStates, getFieldId } from "../reducers/form-state"
import { compareFieldValue, evaluateState, updateFormExpressionState } from "../form/form-state"

export const SCOPE_SET = "SCOPE_SET";

function updateScopeDependencies(dispatch, state, path)
{
    const { contextDependencies } = getViewModel(state);

    const depsArray = contextDependencies && contextDependencies[path[0]];

    if (depsArray)
    {
        const componentUpdates = {};

        // first create a copy of all vars involved
        for (let i = 0; i < depsArray.length; i++)
        {
            const obj = depsArray[i];

            if (!componentUpdates[obj.componentId])
            {
                componentUpdates[obj.componentId] = {... getComponentVars(state, obj.componentId)};
            }
        }

        // then null all dependency varNames. This is only needed if the vars reference to a scope variable
        // comes from a default value, but we don't know whether this is the case, so we do it in any case
        for (let i = 0; i < depsArray.length; i++)
        {
            const obj = depsArray[i];
            componentUpdates[obj.componentId][obj.varName] = null;
        }

        let promise = null;

        for (let componentId in componentUpdates)
        {
            if (componentUpdates.hasOwnProperty(componentId))
            {
                const vars = componentUpdates[componentId];

                if (!promise)
                {
                    promise = dispatch(
                        updateComponent(componentId, vars, true)
                    );
                }
                else
                {
                    promise = promise.then(() => dispatch(
                        updateComponent(componentId, vars, true)
                    ));
                }
            }
        }
    }
}


function updateFormValues(dispatch, state, changed)
{
    const contextCache = {};

    const newFieldValues = {};
    const newFieldStates = {};
    let valuesChanged = false;
    let statesChanged = false;

    const blocks = getFormBlocks(state);

    for (let blockId in blocks)
    {
        if (blocks.hasOwnProperty(blockId))
        {
            const block = blocks[blockId];
            newFieldStates[blockId] = evaluateState(block.model, block.parentBlockId, newFieldStates, contextCache, state);
        }
    }

    const fields = getFields(state);


    const currentFieldValues = getFieldValues(state);
    const currentFieldStates = getFieldStates(state);

    for (let modelId in fields)
    {
        if (fields.hasOwnProperty(modelId))
        {
            const field = fields[modelId];

            // copy cursor path for mutation
            const cursorPath = field.cursorPath && field.cursorPath.slice();

            if (cursorPath && cursorPath[0] === changed)
            {
                const iterationIndex = field.iterationIndex;
                if (iterationIndex >= 0)
                {
                    const scopeGraph = getScopeGraph(state);

                    const arrayCursor = new DataCursor(getDomainData(state), scopeGraph, cursorPath.slice(0, iterationIndex));

                    const array = arrayCursor.get();
                    const length = array.length;
                    for (let i = 0; i < length; i++)
                    {
                        cursorPath[iterationIndex] = i;

                        const fieldId = getFieldId(state, modelId, cursorPath);
                        const currentValue = currentFieldValues[fieldId];

                        const newValue = compareFieldValue(field, i, contextCache, state, currentValue);

                        if (newValue !== null)
                        {
                            newFieldValues[fieldId] = newValue;
                            valuesChanged = true;
                        }

                        const newFieldState = evaluateState(field.model, field.parentBlockId, newFieldStates, contextCache, state);
                        if (currentFieldStates[fieldId] !== newFieldState)
                        {
                            newFieldStates[fieldId] = newFieldState;
                            statesChanged = true;
                        }
                    }
                }
                else
                {

                    const currentValue = currentFieldValues[modelId];
                    const newValue = compareFieldValue(field, 0, contextCache, state, currentValue);
                    if (newValue !== null)
                    {
                        newFieldValues[modelId] = newValue;
                        valuesChanged = true;
                    }

                    const newFieldState = evaluateState(field.model, field.parentBlockId, newFieldStates, contextCache, state);
                    if (currentFieldStates[modelId] !== newFieldState)
                    {
                        newFieldStates[modelId] = newFieldState;
                        statesChanged = true;
                    }
                }
            }
            else
            {
                const iterationIndex = field.iterationIndex;
                if (iterationIndex >= 0)
                {
                    const scopeGraph = getScopeGraph(state);

                    const arrayCursor = new DataCursor(getDomainData(state), scopeGraph, cursorPath.slice(0, iterationIndex));

                    const array = arrayCursor.get();
                    const length = array.length;
                    for (let i = 0; i < length; i++)
                    {
                        cursorPath[iterationIndex] = i;

                        const fieldId = getFieldId(state, modelId, cursorPath);

                        const newFieldState = evaluateState(field.model, field.parentBlockId, newFieldStates, contextCache, state);
                        if (currentFieldStates[fieldId] !== newFieldState)
                        {
                            newFieldStates[fieldId] = newFieldState;
                            statesChanged = true;
                        }
                    }
                }
                else
                {
                    const newFieldState = evaluateState(field.model, field.parentBlockId, newFieldStates, contextCache, state);
                    if (currentFieldStates[modelId] !== newFieldState)
                    {
                        newFieldStates[modelId] = newFieldState;
                        statesChanged = true;
                    }
                }
            }
        }
    }

    if (statesChanged || valuesChanged)
    {
        dispatch(
            syncFormFields(
                valuesChanged && newFieldValues,
                statesChanged && newFieldStates
            )
        );
    }
}

export function setScopeGraph(path, graph, value)
{
    return (dispatch, getState) => {

        dispatch({
            type: SCOPE_SET,
            path: path,
            graph: graph,
            // for logging purposes
            value: value
        });

        updateScopeDependencies(dispatch, getState(), path);

        const state = getState();

        updateFormExpressionState(state);

        updateFormValues(dispatch, state, path[0]);
    };
}

/**
 * Action creator to set a scoped value based on a given cursor and a value.
 *
 * @param cursor    {DataCursor} cursor
 * @param value     {*} value
 * @returns {object} action
 */
export function updateScopeCursor(cursor, value)
{
    return setScopeGraph(
        cursor.getPath(),
        cursor.set(value),
        value
    );
}

/**
 * Action creator to set a scoped value based on a scope name and a value.
 *
 * @param path      {Array} cursor path for the scope value target
 * @param value     {*} value
 * @returns {object} action
 */
export function updateScope(path, value)
{
    return (dispatch, getState) => {

        const state = getState();

        const cursor = new DataCursor(
            getDomainData(state),
            getScopeGraph(state),
            path
        );

        if (cursor.get() !== value)
        {
            dispatch(
                setScopeGraph(path, cursor.set(value), value)
            );
        }
    }
}


