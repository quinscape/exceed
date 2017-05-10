import { combineReducers } from "redux"
import { APP_RESET } from "../actions"

import scope from "./scope";
import component from "./component";
import meta from "./meta";
import inpage from "./inpage";

import view from "./view";

const appReducers = combineReducers({

    scope,
    component,
    inpage,

    // READ-ONLY STATE
    meta
});

/**
 * Root reducer
 */
function rootReducer(state,action)
{

    const { type } = action;
    // handle central view update replacement
    if (type === APP_RESET)
    {
        return view(state, action);
    }

    return appReducers(state,action);
}

export * from "./component"
export * from "./scope"
export * from "./meta"
export * from "./inpage"

export default rootReducer;
