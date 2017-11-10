
import { loadEditorView } from "./inpage";
import { getViewModel } from "../reducers/meta";
import { isInPageEditorActive } from "../reducers/inpage";

import hydrate from "../util/hydrate";

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
        const newState = hydrate(state, data);

        const viewModel = getViewModel(data);

        if (isInPageEditorActive(newState))
        {
            dispatch(
                loadEditorView(
                    viewModel
                )
            );
        }


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
