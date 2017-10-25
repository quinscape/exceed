
const HISTORY_SIZE = 30;

export const HISTORY = new Array(HISTORY_SIZE);

let counter = 0;

function record(action, state)
{
    const type = action ? action.type : "@INITIAL";

    const len = HISTORY_SIZE;
    if (counter === len)
    {
        const last = len - 1;

        const recycle = HISTORY[0];

        for (let i=0; i < last; i++)
        {
            HISTORY[ i ] = HISTORY[ i + 1 ];
        }

        recycle.type = type;
        recycle.action = action;
        recycle.state = state;

        HISTORY[last] = recycle;
    }
    else
    {
        HISTORY[ counter++ ] = { type, action, state };
    }
}

/**
 * Simple middleware that keeps a simple reference history of past actions and the resulting new states. The data can
 * be accessed in the browser with "Exceed.history()"
 *
 * @param store
 * @returns {function(*): function(*=)}
 */
export default function(store)
{
    record(null, store.getState());

    return next => action => {

        const newState = next(action);

        record(action, newState);

        return newState;
    }
}
