module.exports = function(s, arg)
{
    if (arg)
    {
        return "[" + s + ":" + Array.prototype.slice.call(arguments, 1) + "]";
    }
    return "[" + s + "]";
};
