/**
 * Debounce helper for components
 *
 * @param func
 * @param time
 */
export default function(func, time)
{
    const debouncedFn = function()
    {
        const args = Array.prototype.slice.call(arguments);

        const timeout = debouncedFn.debouncedTimeout;
        if (timeout)
        {
            clearTimeout(timeout);
        }

        debouncedFn.debouncedTimeout = setTimeout(() =>
        {
            debouncedFn.debouncedTimeout = null;

            func.apply(this, args);

        }, time);
    };

    return debouncedFn;
}
