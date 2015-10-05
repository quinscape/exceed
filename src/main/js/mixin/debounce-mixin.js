var DebounceMixin = {
    debounce: function (func, time)
    {
        var args = Array.prototype.slice.call(arguments, 2);

        var ctx = this;
        var timeout = ctx.debouncedTimeout;
        if (timeout)
        {
            clearTimeout(timeout);
        }

        ctx.debouncedTimeout = setTimeout(function ()
        {
            ctx.debouncedTimeout = null;
            func.apply(ctx, args);

        } , time);
    }
};

module.exports = DebounceMixin;
