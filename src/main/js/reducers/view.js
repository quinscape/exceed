import assign from "object-assign"

import { APP_RESET } from "../actions"

export default function view(state, action)
{
    const { type } = action;

    if (type === APP_RESET)
    {
        // merge update with current state
        const newStateFromAction = action.viewData;

        const mergedState = assign(
            {},
            state,
            newStateFromAction
        );

        // merge meta separately
        mergedState.meta = assign({}, state.meta, newStateFromAction.meta);

        return mergedState;
    }

    return state;
}
