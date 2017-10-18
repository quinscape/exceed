
let lookupTemplate;

if (__SERVER)
{
    lookupTemplate = global.__lookup_i18n;
}
else
{
    const getTranslationTemplate = require("../reducers/meta").getTranslationTemplate;
    //console.log(getTranslationTemplate);
    const store = require("./store").default;

    lookupTemplate = function (name)
    {
        return getTranslationTemplate( store.getState(), name);
    }
}


const EMPTY = [];

function format(tag, arg)
{
    return tag.replace(/\{([0-9]+)}/g, function (m, nr)
    {
        return arg[+nr];
    });
}

/**
 * Returns a translation of the given translation key with additional optional arguments
 * @param s {string} translation tag/key
 * @param arg {?string} translation tag/key
 * @returns {*}
 */
export default function(s, arg)
{
    const result = lookupTemplate(s);
    if (result !== undefined)
    {
        if (arg !== undefined)
        {
            return format(result, Array.prototype.slice.call(arguments, 1));
        }
        else
        {
            return format(result, EMPTY);
        }
    }

    const colonPos = s.indexOf(':');

    if (colonPos >= 0)
    {
        s = s.substr( colonPos + 1);
    }

    if (arg !== undefined)
    {
        return "[" + format(s, Array.prototype.slice.call(arguments, 1)) + "]"
    }

    return "[" + s + "]";
};
