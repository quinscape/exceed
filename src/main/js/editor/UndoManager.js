
import keys from "../util/keys"

import store from "../service/store"

import { restoreEditorState, EDITOR_STATE_RESTORE } from "../actions/editor"

// last valid index in the undo buffer
export const MAX_UNDO = 63;

export class UndoManager
{
    constructor(store, nonUndoable)
    {
        this.store = store;
        this.data = new Array(MAX_UNDO + 1);

        this.pos = 0;
        this.last = 0;
        this.clean = 0;

        this.nonUndoable = nonUndoable;
    }

    canUndo()
    {
        return this.pos > 0;
    }

    undo = () =>
    {
        if (this.canUndo())
        {
            this.store.dispatch(
                restoreEditorState(this.data[--this.pos])
            )
        }
    };

    isClean()
    {
        return this.pos === this.clean;
    }

    markClean()
    {
        this.clean = this.pos;
    }

    canRedo()
    {
        return this.pos < this.last;
    }

    getCleanState()
    {
        return this.data[this.clean];
    }

    redo = () =>
    {
        if (this.canRedo())
        {
            this.store.dispatch(
                restoreEditorState(this.data[++this.pos])
            )
        }
    };

    update(state)
    {
        this.data[this.pos] = state;
    }

    insert(state)
    {

        if (this.pos === MAX_UNDO)
        {
            for (let i = 1; i <= MAX_UNDO; i++)
            {
                this.data[i - 1] = this.data[i];
            }
            this.data[this.pos] = state;
        }
        else
        {
            this.data[++this.pos] = state;
            this.last = this.pos;
        }
    }

    middleWare = (store) =>
    {
        const sliceNames = keys(store.getState()).filter( name => name !== this.nonUndoable);

        //console.log("Undoable slices: ", sliceNames);

        return next => action => {

            const oldState = store.getState();
            const result = next(action);
            const newState = store.getState();

            if ( action.type !== EDITOR_STATE_RESTORE )
            {
                for (let i = 0; i < sliceNames.length; i++)
                {
                    const name = sliceNames[i];
                    if (oldState[name] !== newState[name])
                    {
                        //console.log("Insert undo");
                        this.insert(newState);
                        return result;
                    }
                }
            }

            //console.log("Update undo");
            this.update(newState);
            return result;
        }
    }
}

export default new UndoManager(store, "editorView");
