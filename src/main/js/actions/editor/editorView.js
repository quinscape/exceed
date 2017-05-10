import assign from "object-assign"

const hub = require("../../service/hub");

import matchUrl from "../../editor/util/match-url"
import editorNavHistory from "../../editor/nav-history"

import { getEditorView } from "../../reducers/editor/editorView"

export const EDITOR_MODEL_SEARCH_RESULT = "EDITOR_MODEL_SEARCH_RESULT";
export const EDITOR_SET_FILTER = "EDITOR_SET_FILTER";
export const EDITOR_NAVIGATE = "EDITOR_NAVIGATE";

export const MIN_SEARCH_LENGTH = 2;

export function searchModel(filter)
{

    return (dispatch, getState) =>
    {
        if (filter.length < MIN_SEARCH_LENGTH)
        {
            dispatch(
                setFilter(filter)
            );
            return;
        }

        return hub.request({
            type: "message.SearchRequest",
            searchTerm: filter
        }).then((results) =>
        {
            console.log(results);

            dispatch({
                type: EDITOR_MODEL_SEARCH_RESULT,
                filter,
                results
            })
        });
    }
}

export function navigateEditor(location)
{
    return (dispatch, getState) => {

        dispatch({
            type: EDITOR_NAVIGATE,
            location
        });

        editorNavHistory.newState(getEditorView(getState()), location.uri);

    }
}

export function setFilter(filter = "")
{
    return {
        type: EDITOR_SET_FILTER,
        filter
    };
}

export function syncEditor()
{
    const locationFromUrl = matchUrl(location.href + location.search);

    return navigateEditor(locationFromUrl);
}
