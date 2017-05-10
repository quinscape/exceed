/**
 * Call the given change callback whenever the given selector produces
 * a different value from the store
 *
 * @param store         store
 * @param select        selector function
 * @param onChange      change callback
 */
export default function observeStore(store, select, onChange)
{
    let currentState;

    function handleChange()
    {
        let nextState = select(store.getState());
        if (nextState !== currentState)
        {
            currentState = nextState;
            onChange(currentState);
        }
    }

    return store.subscribe(handleChange);
}
