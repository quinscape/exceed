import assign from "object-assign"

import {
        EDITOR_LOAD_VIEW,
        EDITOR_TOGGLE,
        EDITOR_UPDATE_CONTENT,
        EDITOR_SELECT_CONTENT,
        EDITOR_SELECT_DOCUMENT,
        EDITOR_MARK_SAVED
    } from "../actions/inpage"

import update from "react-addons-update"

/**
 * Number of views after which the editor will begin reusing document positions even if they do not belong to the
 * same view.
 *
 * @type {number}
 */

const DEFAULT_STATE = {
    active: false,
    documents: [],
    currentDocument: 0
};

/**
 * Slice reducer for the in-page editor part of the application in development mode.
 *
 * The main exceed editor app has its own redux store.
 *
 * @param state
 * @param action
 */
export default function(state = DEFAULT_STATE, action)
{
    switch(action.type)
    {
        case EDITOR_LOAD_VIEW:
        {
            const index = action.index;
            return update(
                state,
                {
                    documents: {
                        [index] : { $set: action.document}
                    },
                    currentDocument : { $set: index }
                }
            );
        }

        case EDITOR_TOGGLE:
        {

            const newState = assign({}, state);
            newState.active = !state.active;
            return newState;

        }

        case EDITOR_UPDATE_CONTENT:
        {
            const currentIndex = state.currentDocument;
            const document = state.documents[currentIndex];
            const contentName = action.contentName;
            const contentModel = action.contentModel;

            return update(
                state,
                {
                    documents: {
                        [currentIndex] : { $set: document.withContentModel(contentName, contentModel) }
                    }
                }
            );
        }

        case EDITOR_SELECT_DOCUMENT:
        {
            const newState = assign({}, state);
            newState.currentDocument = action.index;
            return newState;
        }

        case EDITOR_SELECT_CONTENT:
        {

            const currentIndex = state.currentDocument;
            const currentDocument = state.documents[currentIndex];
            const contentName = action.contentName;

            return update(
                state,
                {
                    documents: {
                        [currentIndex] : { $set: currentDocument.withCurrentContent(contentName) }
                    }
                }
            );
        }

        case EDITOR_MARK_SAVED:
        {

            const currentIndex = state.currentDocument;
            const currentDocument = state.documents[currentIndex];

            return update(
                state,
                {
                    documents: {
                        [currentIndex] : { $set: currentDocument.markClean() }
                    }
                }
            );
        }


        default:
            return state;
    }
}

export function isInPageEditorActive(state)
{
    return getViewEditorState(state).active;
}

export function isViewEditorClean(state)
{
    const documents = getViewDocuments(state);
    for (let i = 0; i < documents.length; i++)
    {
        let doc = documents[i];
        if (!doc.isClean())
        {
            return false;
        }
    }
    return true;
}

export function getViewEditorState(state)
{
    return state.inpage;
}


export function getViewDocuments(state)
{
    return getViewEditorState(state).documents;
}

/**
 * Returns the current editor document from the state.
 *
 * @param state
 * @returns {EditorViewDocument} current edit document
 */
export function getCurrentViewDocument(state)
{
    return getViewDocument(state, getCurrentViewDocumentIndex(state));
}

/**
 * Returns the index of the qualified view name in the currently loaded in-page editor documents or -1 if the view
 * is not loaded.
 *
 * @param state                 state
 * @param qualifiedViewName     qualified view name
 * @returns {number} index or -1
 */
export function findViewDocumentIndex(state, qualifiedViewName)
{
    const documents = getViewDocuments(state);

    for (let i = 0; i < documents.length; i++)
    {
        const doc = documents[i];

        if (doc.model.name === qualifiedViewName)
        {
            return i;
        }
    }
    return -1;
}

/**
 * Returns the editor document with the given index from the state.
 *
 * @param state     state
 * @param index     document index
 * @returns {EditorViewDocument} edit document at that index or null
 */
export function getViewDocument(state, index)
{
    return getViewDocuments(state)[index] || null;
}

export function getCurrentViewDocumentIndex(state)
{
    return getViewEditorState(state).currentDocument;
}

/**
 * Returns the current unsaved editor changes.
 *
 * @param state
 *
 * @returns {object} object mapping qualified view names to view JSON.
 */
export function findCurrentViewChanges(state)
{
    const changes = {};

    const documents = getViewDocuments(state);
    let haveChanges = false;

    for (let i = 0; i < documents.length; i++)
    {
        const doc = documents[i];
        if (!doc.isClean())
        {
            const { model } = doc;

            changes[model.name] = JSON.stringify(model);
            haveChanges = true;
        }
    }

    return haveChanges && changes;
}
