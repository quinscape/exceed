
/**
 * Returns an weakmap based object memoizer function that will cache a value for the given object identity
 * or produce it by calling the given producer function.
 *
 * The obj is kept in the WeakMap, thus can be garbage collected.
 *
 * @returns {Function}
 */
export default function createMemoizer(producer)
{
    let map = new WeakMap();

    /**
     * Memoizer function
     *
     * @param obj       base object
     * @param varargs   {...*} additional args which must be constant over the lifetime of the base object
     */
    return function (obj, varargs)
    {
        if (obj === undefined)
        {
            map = new WeakMap();
            return;
        }

        const existing = map.get(obj);
        if (existing)
        {
            return existing;
        }

        const newValue = producer.apply(null, arguments);
        map.set(obj, newValue);
        return newValue;
    }
}

