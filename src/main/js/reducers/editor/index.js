import { combineReducers } from "redux"

import meta, { getMeta } from "../meta";

import editor from "./editor"
import editorView from "./editorView"

import { EDITOR_STATE_RESTORE, EDITOR_UNDO_GROUP } from "../../actions/editor"

export * from "./editorView"
export * from "./editor"
                                                
export * from "./meta";

const editorReducers = combineReducers({

    // current editor view state (what object is selected, which editor shown etc)
    editorView,
    // complex editor data graph state
    editor,
    // meta block
    meta
});


export default function (state, action)
{
    if (action.type === EDITOR_STATE_RESTORE)
    {
        return action.state;
    }

    if (action.type === EDITOR_UNDO_GROUP)
    {
        const { actions } = action;

        for (let i = 0; i < actions.length; i++)
        {
            const action = actions[i];
            state = editorReducers(state, action);
        }

        return state;
    }

    return editorReducers(state, action);
}
