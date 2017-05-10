import assert from "power-assert";
import sinon from "sinon";
import assign from "object-assign";

import { UndoManager } from "../../../main/js/editor/UndoManager"
import { EDITOR_STATE_RESTORE } from "../../../main/js/actions/editor"

describe("UndoManager", function ()
{

    let state = {
        // non-undoable slice
        no: 0,
        // undoable slice
        yes: 1
    };

    const store = {
        getState : function ()
        {
            return state;
        },
        dispatch: function(action)
        {
            // mock redux.
            // either executes the restore or merges the action onto the state

            // issued by .undo() and .redo()
            if (action.type === EDITOR_STATE_RESTORE)
            {
                state = action.state;
            }
            else
            {
                state = assign({}, state, action);
            }

            return action;
        }
    };
    const manager = new UndoManager(store, "no");
    manager.update(state);

    const disp = manager.middleWare(store)(store.dispatch);
    
    it("records state changes", function ()
    {

        assert( state.no === 0);
        assert( state.yes === 1);
        assert( manager.pos === 0);
        assert( manager.isClean());
        assert( !manager.canUndo());
        assert( !manager.canRedo());

        disp({no: 1});

        assert( state.no === 1);
        assert( state.yes === 1);
        assert( manager.pos === 0);
        assert( manager.isClean());
        assert( !manager.canUndo());
        assert( !manager.canRedo());

        disp({yes: 2});

        assert( state.no === 1);
        assert( state.yes === 2);
        assert( manager.pos === 1);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( !manager.canRedo());

    });

    it("undos", function ()
    {
        disp({yes: 4});

        assert( state.no === 1);
        assert( state.yes === 4);
        assert( manager.pos === 2);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( !manager.canRedo());

        manager.undo();

        assert( state.no === 1);
        assert( state.yes === 2);
        assert( manager.pos === 1);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( manager.canRedo());

        disp({yes: 3});
        assert( state.no === 1);
        assert( state.yes === 3);
        assert( manager.pos === 2);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( !manager.canRedo());

        manager.undo();

        assert( state.no === 1);
        assert( state.yes === 2);
        assert( manager.pos === 1);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( manager.canRedo());

        manager.undo();

        assert( state.no === 1);
        assert( state.yes === 1);
        assert( manager.pos === 0);
        assert( manager.isClean());
        assert( !manager.canUndo());
        assert( manager.canRedo());

        manager.redo();
        manager.undo();

        assert( state.no === 1);
        assert( state.yes === 1);
        assert( manager.pos === 0);
        assert( manager.isClean());
        assert( !manager.canUndo());
        assert( manager.canRedo());

    });

    it("redos", function ()
    {
        manager.redo();

        assert( state.no === 1);
        assert( state.yes === 2);
        assert( manager.pos === 1);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( manager.canRedo());

        manager.redo();

        assert( state.no === 1);
        assert( state.yes === 3);
        assert( manager.pos === 2);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( !manager.canRedo());

        manager.undo();
        manager.redo();

        assert( state.no === 1);
        assert( state.yes === 3);
        assert( manager.pos === 2);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( !manager.canRedo());
    });

    it("resets on insert", function ()
    {
        // if there are redos beyond our current position, inserting will kill them.

        // we gonna lose this state
        assert( state.no === 1);
        assert( state.yes === 3);
        assert( manager.pos === 2);
        assert( !manager.isClean());
        assert( manager.canUndo());
        assert( !manager.canRedo());

        manager.undo();
        manager.undo();

        // back to start
        assert( state.no === 1);
        assert( state.yes === 1);
        assert( manager.pos === 0);
        assert( manager.isClean());
        assert( !manager.canUndo());
        assert( manager.canRedo());

        disp({yes: 5});

        assert( state.no === 1);
        assert( state.yes === 5);
        assert( manager.pos === 1);
        assert( !manager.isClean());
        assert( manager.canUndo());

        // cannot redo anymore
        assert( !manager.canRedo());
    });

});
