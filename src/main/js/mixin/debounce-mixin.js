var DebounceMixin = {
    debounce: function (func, time)
    {
        var args = Array.prototype.slice.call(arguments, 2);

        var timeout = this.debouncedTimeout;
        if (timeout)
        {
            clearTimeout(timeout);
        }

        this.debouncedTimeout = setTimeout(() =>
        {
            this.debouncedTimeout = null;
            func.apply(this, args);

        } , time);
    }
};

module.exports = DebounceMixin;
