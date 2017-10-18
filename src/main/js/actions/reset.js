import assign from "object-assign";

import { loadEditorView } from "./inpage";
import { getViewModel } from "../reducers/meta";
import { isInPageEditorActive } from "../reducers/inpage";
import { convertComponents, convert } from "../util/convert";
import { newFormState, prepareViewModel } from "../form/form-state";

export const APP_RESET = "APP_RESET";

/**
 * Initializes the exceed redux store with a new state from the server. Parts of the state are converted, form-state is
 * prepared, etc.
 *
 * This should happen only once for every state.
 *
 * @param data
 */
export function hydrateAppState(data)
{
    return (dispatch, getState) =>
    {
        const state = getState();
        const viewModel = getViewModel(data);

        if (isInPageEditorActive(state))
        {
            dispatch(
                loadEditorView(
                    viewModel
                )
            );
        }


        const newState = {
            component: assign(
                {},
                state.component,
                convertComponents(data.component),
            ),
            scope: {
                dirty: {},
                graph: convert(data.scope.graph)
            },
            // merge meta
            meta: assign(
                {},
                state.meta,
                data.meta
            ),
            inpage: state.inpage
        };

        prepareViewModel(newState);

        newState.formState = newFormState(newState);

        dispatch({
            type: APP_RESET,
            newState: newState
        });


    };
}

/**
 * Restores an already hydrated state
 *
 * @param data      hydrated state
 */
export function restoreAppState(data)
{
    return (dispatch, getState) => {

        const state = getState();
        const viewModel = getViewModel(data);

        if (isInPageEditorActive(state))
        {
            dispatch(
                loadEditorView(
                    viewModel
                )
            );
        }

        dispatch({
            type: APP_RESET,
            newState: data
        });
    }
}
