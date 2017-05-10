export const EDITOR_LOAD_VIEW = "EDITOR_LOAD_VIEW";
export const EDITOR_TOGGLE = "EDITOR_TOGGLE";
export const EDITOR_UPDATE_CONTENT = "EDITOR_UPDATE_CONTENT";
export const EDITOR_SELECT_CONTENT = "EDITOR_SELECT_CONTENT";
export const EDITOR_SELECT_DOCUMENT = "EDITOR_SELECT_DOCUMENT";
export const EDITOR_MARK_SAVED = "EDITOR_MARK_SAVED";

import { getViewDocuments, findViewDocumentIndex, isInPageEditorActive, getCurrentViewDocumentIndex } from "../reducers/inpage"
import { refetchView } from "../actions/view"
import { getViewModel } from "../reducers/meta"

import EditorViewDocument from "../editor/code/EditorDocument"

import aceLoader from "../editor/ace-loader"

export const EDITOR_ACTIVE_KEY = "Exceed_Editor_Active";

const DOCUMENT_RECYCLE_LIMIT = 8;

const hub = require("../service/hub");

function docIsClean(doc)
{
    return doc.isClean();
}

function sortByChanged(a,b)
{
    // sort in change time order, oldest first
    return a.changed.getTime() - b.changed.getTime()
}

function findLeastRecentlyUsedAndClean(documents)
{
    let minTime = Infinity;
    let index = -1;

    for (let i = 0; i < documents.length; i++)
    {
        const doc = documents[i];
        if (doc.isClean())
        {
            const time = doc.changed.getTime();
            if (time < minTime)
            {
                minTime = time;
                index = i;
            }
        }
    }

    return index;
}

export function loadEditorView(viewModel)
{
    return (dispatch, getState) => {

        const state = getState();
        const documents = getViewDocuments(state);
        const numberOfDocuments = documents.length;

        // we default to adding a new element to the documents array
        let index = numberOfDocuments;

        const name = viewModel.name;

        // do we already edit the view to be loaded?
        const documentIndex = findViewDocumentIndex(state, name);
        if (documentIndex >= 0)
        {
            //console.log("Already loaded");

            if (getCurrentViewDocumentIndex(state) !== documentIndex)
            {
                // display already loaded editor document again
                dispatch(
                    selectEditorDocument(documentIndex)
                );
            }
            return;
        }

        // do we already recycle view slots?
        if (numberOfDocuments < DOCUMENT_RECYCLE_LIMIT)
        {
            // nope -> add it to the end
            index = numberOfDocuments;
        }
        else
        {
            // yes -> find least recently used document that is also clean
            const idx = findLeastRecentlyUsedAndClean(documents);
            if (idx >= 0)
            {
                // reuse the index
                index = idx;
            }
        }

        return aceLoader.load().then(ace => {

            const document = new EditorViewDocument(ace, viewModel);

            dispatch({
                type: EDITOR_LOAD_VIEW,
                document,
                index
            });
        });
    }
}


export function toggleEditor()
{
    return (dispatch, getState) => {

        const state = getState();

        dispatch(
            loadEditorView(
                getViewModel(state)
            )
        );

        const newValue = !isInPageEditorActive(state);
        sessionStorage.setItem(EDITOR_ACTIVE_KEY, String(newValue));

        dispatch({
            type: EDITOR_TOGGLE,
            active: newValue
        });
    };
}

export function updateContentModel(contentName, contentModel)
{
    return {
        type: EDITOR_UPDATE_CONTENT,
        contentName,
        contentModel
    };
}

export function selectEditorContentModel(contentName)
{
    return {
        type: EDITOR_SELECT_CONTENT,
        contentName
    };
}

export function selectEditorDocument(index)
{
    return {
        type: EDITOR_SELECT_DOCUMENT,
        index
    };
}

export function markEditorDocumentSaved()
{
    return {
        type: EDITOR_MARK_SAVED
    };
}

export function saveViewModels(models)
{
    return (dispatch, getState) => {

        const map = {};
        for (let i = 0; i < models.length; i++)
        {
            const model = models[i];
            map[model.name] = JSON.stringify(model, null, 4);
        }

        return hub.request({
            type: "message.SaveViewRequest",
            documents: map
        }).then((data) =>
        {
            dispatch(
                markEditorDocumentSaved()
            );

            dispatch(
                refetchView()
            ); 
            return data;
        })
        .catch(function (err)
        {
            console.error(err);
        });
    }
}
