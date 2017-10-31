import isArray from "../../src/main/js/util/is-array"

/**
 * Creates a sort function that sorts elements by the given order of strings. If an element is not contained in the
 * order array, it is assigned a default priority relative to it.
 *
 * @param order         order array
 * @param defaultCmp    if positive sort undefined values after the order array, if negative, before the array.
 * @param nameProvider  function to provide the sort name for complex objects, default is to sort the objects as strings themselves
 * @returns {function} sort function
 */
export default function(order, defaultCmp = 1, nameProvider)
{
    if (!isArray(order))
    {
        return undefined;
    }

    const map = {};

    const len = order.length;

    const defaultPriority = defaultCmp >= 0 ? len + 1 : -1;

    for (let i = 0; i < len; i++)
    {
        map[order[i]] = i + 1;
    }

    return function(a,b)
    {
        a = nameProvider ? nameProvider(a) : a;
        b = nameProvider ? nameProvider(b) : b;

        const priorityA = map[a] || defaultPriority;
        const priorityB = map[b] || defaultPriority;

        const delta = priorityA - priorityB;
        if (delta === 0)
        {
            return a.localeCompare(b);
        }
        else
        {
            return delta;
        }
    }
}
