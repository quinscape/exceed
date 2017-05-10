import assign from "object-assign"

/**
 * The editorView slice contains all the exceed main editor data that is not part of the undo/redo mechanisms.
 *
 * In essence, this slice of the view describes the current view mode inside the editor, independent from the application
 * model state in the rest.
 */

import {
    EDITOR_MODEL_SEARCH_RESULT,
    EDITOR_SET_FILTER,
    EDITOR_NAVIGATE,
    EDITOR_RESET_VIEW,
    
    MIN_SEARCH_LENGTH
} from "../../actions/editor"

const DEFAULT_EDITOR_VIEW = {
    filter : "",
    location: {
        type: "config",
        name: null,
        resultType: null,
        detail: null
    },
    results: []
};

export default function(state = DEFAULT_EDITOR_VIEW, action)
{
    switch(action.type)
    {
        case EDITOR_SET_FILTER:
        {
            const newState = assign({}, state);
            const filter = action.filter;
            newState.filter = filter;
            if (filter.length < MIN_SEARCH_LENGTH)
            {
                newState.results = [];
            }
            return newState;
        }
        
        case EDITOR_MODEL_SEARCH_RESULT:
        {
            const newState = assign({}, state);

            newState.filter = action.filter;
            newState.results = action.results;

            return newState;
        }

        case EDITOR_NAVIGATE:
        {
            const newState = assign({}, state);
            newState.location = action.location;

            return newState;
        }

        case EDITOR_RESET_VIEW:
        {
            return action.editorView;
        }
    }

    return state;
}

export function getEditorView(state)
{
    return state.editorView;
}

export function getFilter(state)
{
    return getEditorView(state).filter;
}

export function getSearchResults(state)
{
    return getEditorView(state).results;
}

export function getCurrentEditorLocation(state)
{
    return getEditorView(state).location;
}


