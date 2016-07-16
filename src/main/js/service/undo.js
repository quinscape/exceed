const haveWindow = typeof window !== "undefined" && typeof window.addEventListener === "function";
var Mousetrap;
var Event = require("../util/event");
if (haveWindow)
{
    Mousetrap = require("mousetrap");
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

function undo()
{
//    console.log("BEFORE UNDO-PTR", this.ptr);
    if (this.canUndo())
    {
        this.stateCallback(this.states[--this.ptr]);

//        console.log("UNDO-PTR", this.ptr);
    }
}

function redo()
{
//    console.log("REDO-PTR", this.ptr, this.states.length);
    if (this.canRedo())
    {
        this.stateCallback(this.states[++this.ptr])
    }
}

function revert()
{
    //console.log("REDO-PTR", this.ptr, this.states.length);
    if (!this.isSaved())
    {
        this.ptr = this.saved;
        this.stateCallback(this.states[this.ptr])
    }
}

/**
 * Undo service handling all the undo/redo functionality we provide. Some widgets (like the CodeEditor) use third-party
 * editing tools which implement their own undo/redo.
 *
 * The undo service works under the assumption that the states it handles are immutable / copied when changed.
 */

function UndoHandler(initialState, cb)
{
    this.states = [initialState];
    this.ptr = 0;
    this.saved = 0;

    this.stateCallback = cb;

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
    return this.ptr === this.saved;
};

UndoHandler.prototype.getSavedState = function ()
{
    return this.states[this.saved];
};

UndoHandler.prototype.newState = function (state)
{
    if (this.ptr < this.states.length - 1)
    {
        this.states = this.states.slice(0, this.ptr + 1);
    }

    this.states[++this.ptr] = state;
    this.stateCallback(state);

//    console.log("NEW-STATE", this.states, this.ptr, this.saved);
};

UndoHandler.prototype.markSaved = function ()
{
    this.saved = this.ptr;
    this.stateCallback(this.states[this.saved]);
};

module.exports = {
    UndoHandler: UndoHandler,
    /**
     * Creates a new undo handler
     *
     * @param initialState          initial state
     * @param cb                    call back to be
     * @returns {UndoHandler}
     */
    create: function (initialState, cb)
    {
        return new UndoHandler(initialState, cb);
    }
};
