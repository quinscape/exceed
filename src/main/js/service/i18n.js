

function format(tag, arg)
{
    return tag.replace(/\{([0-9]+)}/g, function (m, nr)
    {
        return arg[+nr];
    });
}

module.exports = function(s, arg)
{
    var viewService = require("./view");

    if (!viewService || typeof viewService.getRuntimeInfo !== "function")
    {
        throw new Error("View Service not initialized");
    }

    var translations = viewService.getRuntimeInfo().translations;

    var result = translations[s];
    if (result !== undefined)
    {
        if (arg)
        {
            return format(result, Array.prototype.slice.call(arguments, 1));
        }
        else
        {
            return format(result, []);
        }
    }

    var colonPos = s.indexOf(':');

    if (colonPos >= 0)
    {
        s = s.substr( colonPos + 1);
    }

    if (arg)
    {
        return "[" + format(s, Array.prototype.slice.call(arguments, 1)) + "]"
    }

    return "[" + s + "]";
};
