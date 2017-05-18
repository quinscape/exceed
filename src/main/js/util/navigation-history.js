import uuidV4 from "uuid/v4";
import assign from "object-assign"

const DEFAULT_OPTS = {

    // maximum number of history entries kept
    maxHistory : 30,
    // callback to call to restore a previous state on browser navigation
    onRestore: null
};

import Event from "./event";

function newEntry(id, state)
{
    return {
        id: id,
        redux: state
    };
}

/**
 * Helper class to handle browser history changes within exceed apps and editor.
 */
class NavigationHistory
{
    constructor(opts)
    {
        opts = assign({}, DEFAULT_OPTS, opts);

        //console.log("OPTS", opts);

        if (!opts.onRestore)
        {
            throw new Error("Need onRestore option");
        }

        this.opts = opts;


        this.baseId = "-" + uuidV4();

        this.count = 1;
        this.idCount = 0;

    }

    init()
    {
        const { opts } = this;

        this.states = new Array(opts.maxHistory);
        this.states[0] = this.currentState = newEntry(this.nextId(), null);

        if (typeof window !== "undefined")
        {
            Event.add(window, "popstate", this.onPopState);
        }
    }

    destroy()
    {
        if (typeof window !== "undefined")
        {
            Event.remove(window, "popstate", this.onPopState);
        }
    }

    onPopState = ev => {

        const id = ev.state;

        const entry = this.lookupEntry(id);

        if (entry)
        {
            this.currentState = entry;
            this.opts.onRestore(entry.redux);
        }
        else
        {
            this.opts.onRestore(null);
        }
    };
    

    insert(state)
    {
        const entry = newEntry(this.nextId(), state);
        this.states[this.count++] = entry;

        this.currentState = entry;

        if (this.count === this.opts.maxHistory)
        {
            this.count = 0;
        }
    }

    nextId()
    {
        return (this.idCount++) + this.baseId;
    }


    update(state)
    {
        if (!state)
        {
            throw new Error("No state given to update");
        }

        this.currentState.redux = state;

        //console.log("UPDATE", this.currentState);
        window.history.replaceState( this.currentState.id, "Exceed", window.location.href);
    }

    newState(state, url)
    {
        if (!state)
        {
            throw new Error("No state given to update");
        }

        this.insert(state);

        //console.log("NEW-STATE", this.currentState);
        window.history.pushState( this.currentState.id , "Exceed", url || window.location.href);
    }

    lookupEntry(id)
    {
        //console.log("STATES", states);
        const { states } = this;

        for (let i = 0; i < states.length; i++)
        {
            const entry = states[i];
            if (entry && entry.id === id)
            {
                return entry;
            }
        }
        return null;
    }
}

export default NavigationHistory;

