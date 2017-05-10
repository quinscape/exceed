import { createStore, applyMiddleware } from 'redux'

import thunk from 'redux-thunk'

function logger(store)
{
    return next => action => {

        console.log(action.type, {action});

        return next(action);
    }
}

/**
 * Creates a redux store with the given root reducer, initial state and API object
 *
 * @param rootReducer       root reducer
 * @param initialState      {*} initial redux state
 * @param middleWare        {Array} array of additional middleware
 * @returns {Store<S>}
 */
export default function(rootReducer, initialState, middleWare = [])
{
    // required production middle war
    middleWare.push(
        thunk
    );

    if (__DEV)
    {
        const checkUnhandledActions = ( { getState } ) =>
        {
            return next => action => {

                const oldState = getState();
                const result = next(action);
                const newState = getState();

                if (newState === oldState)
                {
                    console.warn("WARNING: Action has caused no state modification:", action);
                }

                return result;
            }
        };

        middleWare.push(
            logger,
            checkUnhandledActions
        );
    }

    return createStore(
        rootReducer,
        initialState,
        applyMiddleware( ... middleWare)
    );
}
