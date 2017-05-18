import { getTranslationTemplate } from "../reducers"
import store from "./store"

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
 * @param s         {string} translation tag/key
 * @param arg       {*...} varargs
 * @returns {*}
 */
export default function(s, arg)
{
    const result = getTranslationTemplate( store.getState(), s);
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
