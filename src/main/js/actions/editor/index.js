export const EDITOR_STATE_RESTORE = "EDITOR_STATE_RESTORE";
export const EDITOR_UNDO_GROUP = "EDITOR_UNDO_GROUP";
export const EDITOR_RESET_VIEW = "EDITOR_RESET_VIEW";

export function restoreEditorState(state)
{
    return {
        type: EDITOR_STATE_RESTORE,
        state
    };
}

export function resetEditorView(editorView)
{
    console.log("RESET TO", editorView);

    return {
        type: EDITOR_RESET_VIEW,
        editorView
    }
}

/**
 * Executes a array of actions so that they're contained in one undo step.
 *
 * @param actions   {Array} array of actions
 *
 * @returns {{type: string, actions: *}}
 */
export function undoGroup(actions)
{
    actions.forEach( action => {
        if (typeof action === "function")
        {
            throw new Error("Only simple, non-thunk actions may be contained in an undo-group")
        }
    });

    return {
        type: EDITOR_UNDO_GROUP,
        actions
    };
}


export * from "./editorView"
export * from "./config"
