import assign from "object-assign"

import { combineReducers } from "redux"
import { APP_RESET } from "../actions"

import scope from "./scope";
import component from "./component";
import formState from "./form-state";
import meta from "./meta";
import inpage from "./inpage";

import resetReducer from "./reset"

const appReducers = combineReducers({

    scope,
    component,
    inpage,
    formState,

    // READ-ONLY STATE
    meta
});

/**
 * Root reducer
 */
function rootReducer(state,action)
{
    if (action.type === APP_RESET)
    {
        return resetReducer(state, action);
    }

    const newState = appReducers(state,action);

    //console.log("NEW-STATE", newState);

    return newState;
}

export * from "./component"
export * from "./scope"
export * from "./meta"
export * from "./inpage"
export * from "./form-state"

export default rootReducer;
