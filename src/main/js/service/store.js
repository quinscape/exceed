let theStore;
let initialState = {
    meta: {
        translations: {
            
        }
    }
};

/**
 * Holds the actual redux store and acts as a proxy to it.
 *
 * Exists to be able to use the correct store reference in view bootup and as third argument to reduct thunks. The module
 * system caches this proxy which then can be lazily initialized with the real store.
 *
 * @name Store
 * @type {{
 *  getState: function,
 *  dispatch: function,
 *  subscribe: function,
 *  replaceReducer: function
 * }}
 */
const StoreHolder = {
    /**
     * Initializes the wrapper redux store and the initial state
     *
     * @param store     store value
     * @param initial   state value
     * @private
     */
    _init: function (store, initial)
    {
        // console.log("INIT", store, initial);
        
        theStore = store;
        initialState = initial;
    },

    /**
     * Returns the current redux state
     * @returns {*} state
     */
    getState: function ()
    {
        if (!theStore)
        {
            // console.log("Using initial state", initialState);
            return initialState
        }
        else
        {
            const state = theStore.getState();
            // console.log("Using store state", state);
            return state;
        }
    },

    /**
     * Dispatches an action.
     *
     * @param action        action
     */
    dispatch: function (action)
    {
        return getStore().dispatch(action);
    },

    /**
     * Subscribes a listener to the store.
     *
     * @param listener
     * @returns unsubscribe function
     */
    subscribe: function (listener)
    {
        return getStore().subscribe(listener);
    },

    /**
     * Replaces the root reducer with a new reducer.
     * 
     * @param reducer
     */
    replaceReducer: function (reducer)
    {
        getStore().replaceReducer(reducer);
    }
};


function getStore()
{
    if (!theStore)
    {
        // this should hopefully never happen, but if it does, someone introduced a module that works with the store
        // before bootup could create it. The dependency in question needs to be delayed, intialized or imported later
        throw new Error("Store not initialized yet.")
    }
    return theStore;
}

export default StoreHolder

