const haveWindow = typeof window !== "undefined" && typeof window.addEventListener === "function";
var Mousetrap;
var Event = require("../util/event");
if (haveWindow)
{
    Mousetrap = require("mousetrap");
}

function noOp()
{
}

function checkSaved(ev)
{
    if (!this.isSaved())
    {
        var message = 'WARNING: Lose unsaved changes in Domain Editor?';
        ev.returnValue = message;
        return message;
    }
}

function undo(done)
{
//    console.log("BEFORE UNDO-PTR", this.ptr);
    if (this.canUndo())
    {
        this.stateCallback(this.states[--this.ptr], done || noOp);

//        console.log("UNDO-PTR", this.ptr);
    }
}

function redo(done)
{
//    console.log("REDO-PTR", this.ptr, this.states.length);
    if (this.canRedo())
    {
        this.stateCallback(this.states[++this.ptr], done || noOp)
    }
}

function revert(done)
{
    //console.log("REDO-PTR", this.ptr, this.states.length);
    if (!this.isSaved())
    {
        this.ptr = this.clean;
        this.stateCallback(this.states[this.ptr], done || noOp)
    }
}

/**
 * Undo service handling all the undo/redo functionality we provide. Some widgets (like the CodeEditor) use third-party
 * editing tools which implement their own undo/redo.
 *
 * The undo service works under the assumption that the states it handles are immutable / copied when changed.
 */

function UndoHandler(initialState, stateCallback)
{
    this.states = [initialState];
    this.ptr = 0;
    this.clean = 0;

    this.stateCallback = stateCallback;

    this.checkSaved = checkSaved.bind(this);
    this.undo = undo.bind(this);
    this.redo = redo.bind(this);
    this.revert = revert.bind(this);

    if (haveWindow)
    {
        Mousetrap.bind("mod+z", this.undo);
        Mousetrap.bind("shift+mod+z", this.redo);

        Event.add(window, "beforeunload", this.checkSaved, true);
    }
    //console.log("INITIAL-STATE", initialState);
}

UndoHandler.prototype.destroy = function (ev)
{
    if (haveWindow)
    {
        Mousetrap.unbind("mod+z", this.undo);
        Mousetrap.unbind("shift+mod+z", this.redo);

        Event.remove(window, "beforeunload", this.checkSaved, true);
    }
};

UndoHandler.prototype.canUndo = function ()
{
    return this.ptr > 0;
};

UndoHandler.prototype.canRedo = function ()
{
    return this.ptr < this.states.length - 1;
};

UndoHandler.prototype.isSaved = function ()
{
    return this.ptr === this.clean;
};

UndoHandler.prototype.getSavedState = function ()
{
    return this.states[this.clean];
};

UndoHandler.prototype.newState = function (state, done)
{
    if (this.ptr < this.states.length - 1)
    {
        this.states = this.states.slice(0, this.ptr + 1);
    }

    this.states[++this.ptr] = state;
    this.stateCallback(state, done || noOp);

//    console.log("NEW-STATE", this.states, this.ptr, this.saved);
};

UndoHandler.prototype.replaceState = function (state, done)
{
    console.log("REPLACE STATE", state);
    this.states[this.ptr] = state;
    this.stateCallback(state, done || noOp);

//    console.log("REPLACED", this.states, this.ptr, this.saved);
};

UndoHandler.prototype.markSaved = function (done)
{
    this.clean = this.ptr;
    this.stateCallback(this.states[this.clean], done || noOp);
};

module.exports = {
    UndoHandler: UndoHandler,
    /**
     * Creates a new undo handler
     *
     * @param initialState          initial state
     * @param stateCallback         call back to be called when a new undo state is created or reactivated and has to be
     *                              reflected as component state
     * @returns {UndoHandler}
     */
    create: function (initialState, stateCallback)
    {
        return new UndoHandler(initialState, stateCallback);
    }
};
